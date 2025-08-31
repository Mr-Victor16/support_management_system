package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import com.projekt.repositories.ImageRepository;
import com.projekt.repositories.TicketReplyRepository;
import com.projekt.repositories.TicketRepository;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketControllerOperatorIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("operator", "operator");
        clearDatabase();
    }

    /**
     * Controller method: TicketController.getAllTickets
     * HTTP Method: GET
     * Endpoint: /api/tickets
     * Expected Status: 200 OK
     * Scenario: Retrieving all tickets.
     * Verification: Confirms the returned list size matches the expected ticket count in the repository.
     */
    @Test
    public void getAllTickets_ReturnsTicketListSuccessfully() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(ticketList.size()))
                .log().all();
    }

    /**
     * Controller method: TicketController.getUserTickets
     * HTTP Method: GET
     * Endpoint: /api/tickets/user
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve user tickets as a user without sufficient permissions.
     */
    @Test
    public void getUserTickets_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.getTicketsByUserId
     * HTTP Method: GET
     * Endpoint: /api/tickets/user/{userID}
     * Expected Status: 200 OK
     * Scenario: Retrieving tickets by a valid user ID.
     * Verification: Confirms the returned list size matches the expected ticket count for the given user ID.
     */
    @Test
    public void getTicketsByUserId_ValidId_ReturnsTicketsSuccessfully() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long userID = 1;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .get("/api/tickets/user/{userID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .log().all();
    }

    /**
     * Controller method: TicketController.getTicketsByUserId
     * HTTP Method: GET
     * Endpoint: /api/tickets/user/{userID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Retrieving tickets with an invalid user ID.
     */
    @Test
    public void getTicketsByUserId_InvalidId_ReturnsNotFound() {
        long userID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .get("/api/tickets/user/{userID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with ID " + userID + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.getTicketById
     * HTTP Method: GET
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 200 OK
     * Scenario: Retrieving a ticket by valid ID.
     * Verification: Confirms the response contains the ticket details matching the provided ticket ID.
     */
    @Test
    public void getTicketById_ValidId_ReturnsTicketDetailsSuccessfully() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Ticket ticket = ticketList.get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticket.getId())
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(ticket.getId().intValue()))
                .body("title", equalTo(ticket.getTitle()))
                .body("description", equalTo(ticket.getDescription()))
                .log().all();
    }

    /**
     * Controller method: TicketController.getTicketById
     * HTTP Method: GET
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Retrieving a ticket using an invalid ticket ID.
     */
    @Test
    public void getTicketById_InvalidId_ReturnsNotFound() {
        long ticketID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.addTicket
     * HTTP Method: POST
     * Endpoint: /api/tickets
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add new ticket as a user without sufficient permissions.
     */
    @Test
    public void addTicket_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        Long categoryID = initializeCategory("General").getId();
        Long priorityID = initializePriority("High").getId();

        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", categoryID, priorityID, "1.1", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String newTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newTicketJson)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 200 OK
     * Scenario: Updating a ticket with valid data.
     */
    @Test
    public void updateTicket_ValidData_ReturnsSuccess() throws IOException {
        Long ticketID = initializeTicketForUser(1L).getId();
        Long softwareID = initializeSingleSoftware("Other software name", "Software description").getId();
        Long categoryID = initializeCategory("Question").getId();
        Long priorityID = initializePriority("High").getId();

        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", categoryID, priorityID, "2.1", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket details updated"))
                .log().all();
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 404 NOT FOUND
     * Scenario: Updating a ticket with an invalid category ID.
     */
    @Test
    public void updateTicket_InvalidCategoryId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Ticket ticket = ticketList.get(0);
        long newCategoryID = 1000;
        Long newPriorityID = ticketList.get(2).getPriority().getId();
        Long newSoftwareID = ticketList.get(2).getSoftware().getId();

        UpdateTicketRequest request = new UpdateTicketRequest(ticket.getId(), "Updated title", "Updated description", newCategoryID, newPriorityID, "1.1", newSoftwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Category with ID " + request.categoryID() + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 404 NOT FOUND
     * Scenario: Updating a ticket with an invalid priority ID.
     */
    @Test
    public void updateTicket_InvalidPriorityId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Ticket ticket = ticketList.get(0);
        Long newCategoryID = ticketList.get(2).getCategory().getId();
        long newPriorityID = 1000;
        Long newSoftwareID = ticketList.get(2).getSoftware().getId();

        UpdateTicketRequest request = new UpdateTicketRequest(ticket.getId(), "Updated title", "Updated description", newCategoryID, newPriorityID, "1.1", newSoftwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Priority with ID " + request.priorityID() + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 404 NOT FOUND
     * Scenario: Updating a ticket with an invalid software ID.
     */
    @Test
    public void updateTicket_InvalidSoftwareId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Ticket ticket = ticketList.get(0);
        Long newCategoryID = ticketList.get(2).getCategory().getId();
        Long newPriorityID = ticketList.get(2).getPriority().getId();
        long newSoftwareID = 1000;

        UpdateTicketRequest request = new UpdateTicketRequest(ticket.getId(), "Updated title", "Updated description", newCategoryID, newPriorityID, "1.1", newSoftwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + request.softwareID() + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 404 NOT FOUND
     * Scenario: Updating a ticket with an invalid ticket ID.
     */
    @Test
    public void updateTicket_InvalidTicketId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        long ticketID = 1000;
        Long newCategoryID = ticketList.get(2).getCategory().getId();
        Long newPriorityID = ticketList.get(2).getPriority().getId();
        Long newSoftwareID = ticketList.get(2).getSoftware().getId();

        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", newCategoryID, newPriorityID, "1.1", newSoftwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + request.ticketID() + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.deleteTicket
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 200 OK
     * Scenario: Deleting a ticket with a valid ticket ID.
     * Verification: Confirms the repository count decreases.
     */
    @Test
    public void deleteTicket_ValidTicketId_ReturnsSuccess() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket removed"))
                .log().all();

        assertEquals(ticketRepository.count(), ticketList.size()-1);
    }

    /**
     * Controller method: TicketController.deleteTicket
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to delete a ticket with an invalid ticket ID.
     */
    @Test
    public void deleteTicket_InvalidTicketId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long ticketID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.addImages
     * HTTP Method: POST
     * Endpoint: /api/tickets/{ticketID}/image
     * Expected Status: 200 OK
     * Scenario: Adding an image to a ticket with a valid ticket ID and image file.
     * Verification: Confirms the repository count increases.
     */
    @Test
    public void addImages_ValidTicketId_ReturnsSuccess() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

        long imageNumber = imageRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", ticketID)
                .when()
                .post("/api/tickets/{ticketID}/image")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image added"))
                .log().all();

        assertEquals(imageRepository.count(), imageNumber+1);
    }

    /**
     * Controller method: TicketController.addImages
     * HTTP Method: POST
     * Endpoint: /api/tickets/{ticketID}/image
     * Expected Status: 404 NOT FOUND
     * Scenario: Adding an image to a ticket with an invalid ticket ID.
     */
    @Test
    public void addImages_InvalidTicketId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long ticketID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", ticketID)
                .when()
                .post("/api/tickets/{ticketID}/image")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.deleteImage
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/image/{imageID}
     * Expected Status: 200 OK
     * Scenario: Deleting an image with a valid image ID.
     * Verification: Confirms the repository count decreases.
     */
    @Test
    public void deleteImage_ValidImageId_ReturnsSuccess() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long imageID = ticketList.get(0).getImages().get(0).getId();

        long imageNumber = imageRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image removed"))
                .log().all();

        assertEquals(imageRepository.count(), imageNumber-1);
    }

    /**
     * Controller method: TicketController.deleteImage
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/image/{imageID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Deleting an image with an invalid image ID.
     */
    @Test
    public void deleteImage_InvalidImageId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long imageID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Image with ID " + imageID + " not found."))
                .log().all();
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 200 OK
     * Scenario: Adding a reply to a ticket with a valid ticket ID and reply content.
     * Verification: Confirms the ticket repository count increases.
     */
    @Test
    public void addTicketReply_ValidTicketId_ReturnsSuccess() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

        long replyNumber = ticketReplyRepository.count();

        AddTicketReplyRequest request = new AddTicketReplyRequest(ticketID, "Reply content");
        ObjectMapper objectMapper = new ObjectMapper();
        String addTicketReplyJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addTicketReplyJson)
                .when()
                .post("/api/tickets/reply")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply added"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        assertEquals(ticketReplyRepository.count(), replyNumber+1);
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 404 NOT FOUND
     * Scenario: Adding a reply to a ticket with an invalid ticket ID.
     */
    @Test
    public void addTicketReply_InvalidTicketId_ReturnsNotFound() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long ticketID = 1000;

        AddTicketReplyRequest request = new AddTicketReplyRequest(ticketID, "Reply content");
        ObjectMapper objectMapper = new ObjectMapper();
        String addTicketReplyJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addTicketReplyJson)
                .when()
                .post("/api/tickets/reply")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + request.ticketID() + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 200 OK
     * Scenario: Changing the status of a ticket with valid data.
     */
    @Test
    public void changeTicketStatus_ValidData_ReturnsSuccess() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();
        long statusID = ticketList.get(2).getStatus().getId();

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(ticketID, statusID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket status changed"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 404 NOT FOUND
     * Scenario: Changing the status of a ticket with an invalid ticket ID.
     */
    @Test
    public void changeTicketStatus_InvalidTicketId_ReturnsNotFound() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        long ticketID = 1000;
        long statusID = ticketList.get(0).getStatus().getId();

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(ticketID, statusID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + request.ticketID() + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 404 NOT FOUND
     * Scenario: Changing the status of a ticket with an invalid status ID.
     */
    @Test
    public void changeTicketStatus_InvalidStatusId_ReturnsNotFound() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();
        long statusID = 1000;

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(ticketID, statusID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Status with ID " + request.statusID() + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 200 OK
     * Scenario: Changing the status of a ticket to its current status.
     */
    @Test
    public void changeTicketStatus_NewStatusIsSameCurrentStatus_ReturnsSuccess() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();
        long statusID = ticketList.get(0).getStatus().getId();

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(ticketID, statusID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket status changed"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.deleteTicketReply
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/reply/{replyID}
     * Expected Status: 200 OK
     * Scenario: Deleting a ticket reply with a valid reply ID.
     * Verification: Confirms the reply repository count decreases.
     */
    @Test
    public void deleteTicketReply_ValidReplyId_ReturnsSuccess() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketReplyID = ticketList.get(0).getReplies().get(0).getId();

        long replyNumber = ticketReplyRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", ticketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply removed"))
                .log().all();

        assertEquals(ticketReplyRepository.count(), replyNumber-1);
    }

    /**
     * Controller method: TicketController.deleteTicketReply
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/reply/{replyID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Deleting a ticket reply with an invalid reply ID.
     */
    @Test
    public void deleteTicketReply_InvalidReplyId_ReturnsNotFound() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long ticketReplyID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", ticketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket reply with ID " + ticketReplyID + " not found."))
                .log().all();
    }
}

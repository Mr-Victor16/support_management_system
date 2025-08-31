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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    /**
     * Controller method: TicketController.getAllTickets
     * HTTP Method: GET
     * Endpoint: /api/tickets
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Verifying that the user role cannot access the ticket list.
     */
    @Test
    public void getAllTickets_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.getUserTickets
     * HTTP Method: GET
     * Endpoint: /api/tickets/user
     * Expected Status: 200 OK
     * Scenario: Verifying the status and expected number of tickets assigned to the user.
     * Verification: Confirms the number of tickets in the response is correct.
     */
    @Test
    public void getUserTickets_ReturnsTicketListSuccessfully() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
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
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve ticket list by ID as a user without sufficient permissions.
     */
    @Test
    public void getTicketsByUserId_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long userID = 2;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .get("/api/tickets/user/{userID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.getTicketById
     * HTTP Method: GET
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 200 OK
     * Scenario: Retrieve ticket by ID which author is the user.
     * Verification: Confirms the ticket details match the expected ticket properties.
     */
    @Test
    public void getTicketById_UserTicket_ReturnsTicketDetailsSuccessfully() throws IOException {
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
     * Expected Status: 403 FORBIDDEN
     * Scenario: Verifying the returned status when the ticket author is a different user.
     */
    @Test
    public void getTicketById_OtherUserTicket_ReturnsForbidden() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to access to this ticket"))
                .log().all();
    }

    /**
     * Controller method: TicketController.addTicket
     * HTTP Method: POST
     * Endpoint: /api/tickets
     * Expected Status: 200 OK
     * Scenario: Adding a ticket with valid data.
     * Verification: Confirms that the ticket count in the repository has increased.
     */
    @Test
    public void addTicket_ValidData_ReturnsSuccess() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long categoryID = ticketList.get(0).getCategory().getId();
        Long priorityID = ticketList.get(0).getPriority().getId();

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket added"))
                .log().all();

        assertEquals(ticketRepository.count(), ticketList.size()+1);
    }

    /**
     * Controller method: TicketController.updateTicket
     * HTTP Method: PUT
     * Endpoint: /api/tickets
     * Expected Status: 200 OK
     * Scenario: Update ticket which author is the user.
     */
    @Test
    public void updateTicket_UserTicket_ReturnsSuccess() throws IOException {
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
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to update ticket which author is other user.
     */
    @Test
    public void updateTicket_OtherUserTicket_ReturnsForbidden() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", 1L, 1L, "1.1", 1L);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to update this ticket"))
                .log().all();
    }

    /**
     * Controller method: TicketController.deleteTicket
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 200 OK
     * Scenario: Delete ticket which author is the user.
     * Verification: Confirms the ticket repository count decreases.
     */
    @Test
    public void deleteTicket_UserTicket_ReturnsSuccess() throws IOException {
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
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to delete ticket which author is other user.
     * Verification: Confirms the ticket repository count remains unchanged.
     */
    @Test
    public void deleteTicket_OtherUserTicket_ReturnsForbidden() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

        long ticketNumber = ticketRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to delete this ticket"))
                .log().all();

        assertEquals(ticketRepository.count(), ticketNumber);
    }

    /**
     * Controller method: TicketController.addImages
     * HTTP Method: POST
     * Endpoint: /api/tickets/{ticketID}/image
     * Expected Status: 200 OK
     * Scenario: Add image to a ticket which author is the user.
     * Verification: Confirms the image repository count increases.
     */
    @Test
    public void addImages_UserTicket_ReturnsSuccess() throws IOException {
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
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to add image to a ticket which author is other user.
     * Verification: Confirms the image repository count remains unchanged.
     */
    @Test
    public void addImages_OtherUserTicket_ReturnsForbidden() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

        long imageNumber = imageRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", ticketID)
                .when()
                .post("/api/tickets/{ticketID}/image")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to add image to this ticket"))
                .log().all();

        assertEquals(imageRepository.count(), imageNumber);
    }

    /**
     * Controller method: TicketController.deleteImage
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/image/{imageID}
     * Expected Status: 200 OK
     * Scenario: Delete an image from a ticket which author is the user.
     * Verification: Confirms the image repository count decreases.
     */
    @Test
    public void deleteImage_UserTicket_ReturnsSuccess() throws IOException {
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
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to delete an image from a ticket which author is other user.
     * Verification: Confirms the image repository count remains unchanged.
     */
    @Test
    public void deleteImage_OtherUserTicket_ReturnsForbidden() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long imageID = ticketList.get(1).getImages().get(0).getId();

        long imageNumber = imageRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to delete this image"))
                .log().all();

        assertEquals(imageRepository.count(), imageNumber);
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 200 OK
     * Scenario: Add reply to a ticket which author is the user.
     * Verification: Confirms that the number of ticket replies increases.
     */
    @Test
    public void addTicketReply_UserTicket_ReturnsSuccess() throws MessagingException, IOException {
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

        //Email notification shouldn't be sent because the user is responding in their ticket.
        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        assertEquals(ticketReplyRepository.count(), replyNumber+1);
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to add reply to a ticket which author is the other user.
     * Verification: Confirms the ticket reply repository count remains unchanged.
     */
    @Test
    public void addTicketReply_OtherUserTicket_ReturnsForbidden() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

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
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to add reply to this ticket"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        assertEquals(ticketReplyRepository.count(), replyNumber);
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to add reply to a ticket which is closed.
     * Verification: Confirms the ticket reply repository count remains unchanged.
     */
    @Test
    public void addTicketReply_ClosedTicket_ReturnsForbidden() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(2).getId();

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
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to add reply to closed ticket"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        assertEquals(ticketReplyRepository.count(), replyNumber);
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempt to change a ticket status as a user without sufficient permissions.
     */
    @Test
    public void changeTicketStatus_InsufficientPermissions_ReturnsUnauthorized() throws MessagingException, IOException {
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.deleteTicketReply
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/reply/{replyID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempt to delete a ticket reply as a user without sufficient permissions.
     */
    @Test
    public void deleteTicketReply_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketReplyID = ticketList.get(0).getReplies().get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", ticketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

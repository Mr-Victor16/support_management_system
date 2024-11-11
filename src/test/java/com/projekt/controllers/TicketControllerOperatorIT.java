package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
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

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("operator", "operator");
        clearDatabase();
    }

    //GET: /api/tickets
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllTickets() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //GET: /api/tickets/user
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetUserTickets() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status and expected number of items when the user ID is correct.
    @Test
    public void testGetTicketsByUserId() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //GET: /api/tickets/user/<userID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the user ID is incorrect.
    @Test
    public void testGetTicketsByUserIdWhenIdIsWrong() {
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

    //GET: /api/tickets/<ticketID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the ticket ID is correct.
    @Test
    public void testGetTicketById() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //GET: /api/tickets/<ticketID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testGetTicketByIdWhenIdIsWrong() {
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

    //POST: /api/tickets
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testAddTicketWhenCategoryIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        long categoryID = 1000;
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
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Category with ID " + request.categoryID() + " not found."))
                .log().all();
    }

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testAddTicketWhenPriorityIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long categoryID = ticketList.get(0).getCategory().getId();
        long priorityID = 1000;

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
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Priority with ID " + request.priorityID() + " not found."))
                .log().all();
    }

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testAddTicketWhenSoftwareIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long categoryID = ticketList.get(0).getCategory().getId();
        Long priorityID = ticketList.get(0).getPriority().getId();
        long addTicketRequestSoftwareID = 1000;

        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", categoryID, priorityID, "1.1", addTicketRequestSoftwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String newTicketJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newTicketJson)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + request.softwareID() + " not found."))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdateTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Ticket ticket = ticketList.get(0);
        Long newCategoryID = ticketList.get(2).getCategory().getId();
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket details updated"))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testUpdateTicketWhenCategoryIdIsWrong() throws IOException {
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

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testUpdateTicketWhenPriorityIdIsWrong() throws IOException {
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

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testUpdateTicketWhenSoftwareIdIsWrong() throws IOException {
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

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testUpdateTicketWhenIdIsWrong() throws IOException {
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

    //DELETE: /api/tickets/<ticketID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //DELETE: /api/tickets/<ticketID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testDeleteTicketWhenIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/<ticketID>/image
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddImageToTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

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
    }

    //POST: /api/tickets/<ticketID>/image
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testAddImageToTicketWhenTicketIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //DELETE: /api/tickets/image/<imageID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteImage() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long imageID = ticketList.get(0).getImages().get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image removed"))
                .log().all();
    }

    //DELETE: /api/tickets/image/<imageID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the image ID is incorrect.
    @Test
    public void testDeleteImageWhenIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/reply
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddTicketReply() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

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
    }

    //POST: /api/tickets/reply
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testAddTicketReplyWhenTicketIdIsWrong() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/status
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testChangeTicketStatus() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/status
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testChangeTicketStatusWhenTicketIdIsWrong() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/status
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the status ID is incorrect.
    @Test
    public void testChangeTicketStatusWhenStatusIdIsWrong() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/tickets/status
    //Expected status: OK (200)
    //Purpose: Verify the status returned in case of a change to the current status
    @Test
    public void testChangeTicketStatusToCurrentStatus() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //DELETE: /api/tickets/reply/<replyID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteTicketReply() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketReplyID = ticketList.get(0).getReplies().get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", ticketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply removed"))
                .log().all();
    }

    //DELETE: /api/tickets/reply/<replyID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket reply ID is incorrect.
    @Test
    public void testDeleteTicketReplyWhenIdIsWrong() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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

package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TicketControllerAdminIT extends BaseIntegrationTest {
    private String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
        clearDatabase();
    }

    /**
     * Controller method: TicketController.getAllTickets
     * HTTP Method: GET
     * Endpoint: /api/tickets
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve all tickets as a user without sufficient permissions.
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
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve tickets by user ID as a user without sufficient permissions.
     */
    @Test
    public void getTicketsByUserId_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);
        long userID = 1;

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
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve a ticket by ID as a user without sufficient permissions.
     */
    @Test
    public void getTicketById_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        Long ticketID = initializeTicket(softwareID).get(2).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.addTicket
     * HTTP Method: POST
     * Endpoint: /api/tickets
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add a new ticket as a user without sufficient permissions.
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
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update a ticket as a user without sufficient permissions.
     */
    @Test
    public void updateTicket_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.deleteTicket
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/{ticketID}
     * Expected Status: 401 Unauthorized
     * Scenario: Attempt to delete a ticket as a user without sufficient permissions.
     */
    @Test
    public void deleteTicket_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.addImages
     * HTTP Method: POST
     * Endpoint: /api/tickets/{ticketID}/image
     * Expected Status: 401 Unauthorized
     * Scenario: Attempt to add an image to a ticket as a user without sufficient permissions.
     */
    @Test
    public void addImages_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.deleteImage
     * HTTP Method: DELETE
     * Endpoint: /api/tickets/image/{imageID}
     * Expected Status: 401 Unauthorized
     * Scenario: Attempt to delete an image from a ticket as a user without sufficient permissions.
     */
    @Test
    public void deleteImage_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long imageID = ticketList.get(0).getImages().get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: TicketController.addTicketReply
     * HTTP Method: POST
     * Endpoint: /api/tickets/reply
     * Expected Status: 401 Unauthorized
     * Scenario: Attempt to add a reply to a ticket as a user without sufficient permissions.
     */
    @Test
    public void addTicketReply_InsufficientPermissions_ReturnsUnauthorized() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Controller method: TicketController.changeTicketStatus
     * HTTP Method: POST
     * Endpoint: /api/tickets/status
     * Expected Status: 401 Unauthorized
     * Scenario: Attempt to change ticket status as a user without sufficient permissions.
     */
    @Test
    public void changeTicketStatus_InsufficientPermissions_ReturnsUnauthorized() throws MessagingException, IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(0).getId();
        long statusID = 2;

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
     * Expected Status: 401 Unauthorized
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

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

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    //GET: /api/tickets
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the returned status if the request contains valid data but the user account doesn't have the required permissions.
    @Test
    public void testGetAllTickets() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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
                .body("size()", equalTo(2))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the returned status when the user ID is correct, but the user account doesn't have the required permissions.
    @Test
    public void testGetTicketsByUserId() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);
        long userID = 2;

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user/"+userID)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
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
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testGetTicketByIdWhenTicketAuthorIsOtherUser() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testUpdateTicketWhenTicketAuthorIsOtherUser() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
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
    }

    //DELETE: /api/tickets/<ticketID>
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testDeleteTicketWhenTicketAuthorIsOtherUser() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to delete this ticket"))
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
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testAddImageToTicketWhenTicketAuthorIsOtherUser() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

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
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testDeleteImageWhenTicketAuthorIsOtherUser() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long imageID = ticketList.get(1).getImages().get(0).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("You do not have permission to delete this image"))
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

        //Email notification shouldn't be sent because the user is responding in their ticket.
        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/reply
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testAddTicketReplyWhenTicketAuthorIsOtherUser() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(1).getId();

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
    }

    //POST: /api/tickets/reply
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testAddTicketReplyWhenTicketIsClosed() throws MessagingException, IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long ticketID = ticketList.get(2).getId();

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
    }

    //POST: /api/tickets/status
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the returned status if the request contains valid data but the user account doesn't have the required permissions.
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    //DELETE: /api/tickets/reply/<replyID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the returned status if the request contains valid data but the user account doesn't have the required permissions.
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

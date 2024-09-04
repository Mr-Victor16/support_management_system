package com.projekt.controllers;

import com.projekt.repositories.TicketRepository;
import com.projekt.services.MailService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketControllerOperatorIntegrationTest {
    @LocalServerPort
    private int port;

    private String jwtToken;

    @MockBean
    private MailService mailService;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    public void setUp() throws MessagingException {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        jwtToken = getJwtToken();

        Mockito.doNothing().when(mailService).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mailService).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    private String getJwtToken() {
        String loginJson = "{ \"username\": \"operator\", \"password\": \"operator\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        return response.jsonPath().getString("token");
    }

    @Test
    public void testGetAllTickets() {
        int expectedSize = (int) ticketRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(expectedSize))
                .log().all();
    }

    @Test
    public void testGetUserTickets() {
        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .log().all();
    }

    @Test
    public void testAddTicket() {
        String newTicketJson = "{ \"title\": \"New Ticket\", \"description\": \"Ticket Description\", \"categoryID\": 1, \"priorityID\": 1, \"softwareID\": 1, \"version\": \"1.0\" }";

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
    }

    @Test
    public void testGetTicketById() {
        Long ticketID = 1L;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(ticketID.intValue()))
                .body("title", notNullValue())
                .body("description", notNullValue())
                .log().all();
    }

    @Test
    public void testUpdateTicket() {
        String updateTicketJson = "{ \"ticketID\": 1, \"title\": \"Updated Title\", \"description\": \"Updated Description\", \"categoryID\": 1, \"priorityID\": 1, \"softwareID\": 1, \"version\": \"1.1\" }";

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketJson)
                .when()
                .put("/api/tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket details changed successfully"))
                .log().all();
    }

    @Test
    public void testDeleteTicket() {
        long ticketID = 1;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", ticketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket removed successfully"))
                .log().all();
    }

    @Test
    public void testAddImageToTicket() {
        long ticketID = 1;

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", ticketID)
                .when()
                .post("/api/tickets/{ticketID}/image")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image added successfully"))
                .log().all();
    }

    @Test
    public void testDeleteImage() {
        long imageID = 1;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", imageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image removed successfully"))
                .log().all();
    }

    @Test
    public void testAddTicketReply() throws MessagingException {
        String addTicketReplyJson = "{ \"ticketID\": 1, \"content\": \"Reply content\" }";

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addTicketReplyJson)
                .when()
                .post("/api/tickets/reply")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply added successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @Order(1)
    public void testChangeTicketStatusToCurrentStatus() throws MessagingException {
        String updateTicketStatusJson = "{ \"ticketID\": 1, \"statusID\": 1 }";

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket status changed successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @Order(2)
    public void testChangeTicketStatus() throws MessagingException {
        String updateTicketStatusJson = "{ \"ticketID\": 1, \"statusID\": 2 }";

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateTicketStatusJson)
                .when()
                .post("/api/tickets/status")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket status changed successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeleteTicketReply(){
        long replyID = 1;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", replyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply removed successfully"))
                .log().all();
    }
}

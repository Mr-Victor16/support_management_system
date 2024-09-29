package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.models.Image;
import com.projekt.models.Ticket;
import com.projekt.models.TicketReply;
import com.projekt.payload.request.LoginRequest;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import com.projekt.repositories.*;
import com.projekt.services.MailService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class TicketControllerAdminIntegrationTest {
    @LocalServerPort
    private int port;

    private String jwtToken;
    private Long ticketID;
    private Long imageID;
    private Long ticketReplyID;

    @MockBean
    private MailService mailService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    public void setUp() throws MessagingException, IOException {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        jwtToken = getJwtToken();

        Mockito.doNothing().when(mailService).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mailService).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());

        ticketRepository.deleteAll();

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("The website is unreachable");
        ticket.setStatus(statusRepository.getReferenceById(1L));
        ticket.setPriority(priorityRepository.getReferenceById(2L));
        ticket.setSoftware(softwareRepository.getReferenceById(3L));
        ticket.setUser(userRepository.getReferenceById(1L));
        ticket.setDescription("When trying to enter the site, I get an error - This site is unreachable :(");
        ticket.setVersion("1.0");

        ticket.setCategory(categoryRepository.getReferenceById(1L));

        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(emptyImage, "png", stream);
        Image image = new Image(1L,"1.png",stream.toByteArray());
        imageRepository.save(image);

        List<Image> imageList = new ArrayList<>();
        imageList.add(image);
        ticket.setImages(imageList);

        List<TicketReply> ticketReplyList = new ArrayList<>();
        TicketReply savedReply = ticketReplyRepository.save(new TicketReply(userRepository.getReferenceById(2L),"Have you checked that you have entered the correct address?", LocalDate.of(2021,12,11)));
        ticketReplyList.add(savedReply);
        ticket.setReplies(ticketReplyList);

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketID = savedTicket.getId();
        imageID = savedTicket.getImages().get(0).getId();
        ticketReplyID = savedTicket.getReplies().get(0).getId();
    }

    private String getJwtToken() throws JsonProcessingException {
        LoginRequest request = new LoginRequest("admin", "admin");
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(request);

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

    //GET: /api/tickets
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testGetAllTickets() {
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
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testGetUserTickets() {
        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testGetTicketsByUserId() {
        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //GET: /api/tickets/<ticketID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testGetTicketById() {
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

    //POST: /api/tickets
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testAddTicket() throws JsonProcessingException {
        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", 1L, 1L, "1.1", 1L);
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

    //PUT: /api/tickets
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testUpdateTicket() throws JsonProcessingException {
        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", 2L, 1L, "1.1", 1L);
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

    //DELETE: /api/tickets/<ticketID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testDeleteTicket() {
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

    //POST: /api/tickets/<ticketID>/image
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testAddImageToTicket() {
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

    //DELETE: /api/tickets/image/<imageID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testDeleteImage() {
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

    //POST: /api/tickets/reply
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testAddTicketReply() throws MessagingException, JsonProcessingException {
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

    //POST: /api/tickets/status
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testChangeTicketStatus() throws MessagingException, JsonProcessingException {
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(ticketID, 2L);
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
    //Purpose: Verify the status returned if the request contains valid data. Administrator doesn't have access rights to this method.
    @Test
    public void testDeleteTicketReply(){
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

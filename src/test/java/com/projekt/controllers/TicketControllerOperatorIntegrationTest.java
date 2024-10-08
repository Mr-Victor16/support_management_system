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
import org.junit.jupiter.api.*;
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
public class TicketControllerOperatorIntegrationTest {
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
        LoginRequest request = new LoginRequest("operator", "operator");
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
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
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

    //GET: /api/tickets/user
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetUserTickets() {
        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status and expected number of items when the user ID is correct.
    @Test
    public void testGetTicketsByUserId() {
        long userID = 1;

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user/"+userID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status and expected number of items when the user ID is incorrect.
    @Test
    public void testGetTicketsByUserIdWhenIdIsWrong() {
        long userID = 15;

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/tickets/user/"+userID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with ID " + userID + " not found."))
                .log().all();
    }

    //GET: /api/tickets/<ticketID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the ticket ID is correct.
    @Test
    public void testGetTicketById() {
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

    //GET: /api/tickets/<ticketID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testGetTicketByIdWhenIdIsWrong() {
        long ticketID = 100;

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket added"))
                .log().all();
    }

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testAddTicketWhenCategoryIdIsWrong() throws JsonProcessingException {
        long categoryID = 100;
        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", categoryID, 1L, "1.1", 1L);
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
                .body(equalTo("Category with ID " + categoryID + " not found."))
                .log().all();
    }

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testAddTicketWhenPriorityIdIsWrong() throws JsonProcessingException {
        long priorityID = 100;
        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", 1L, priorityID, "1.1", 1L);
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
                .body(equalTo("Priority with ID " + priorityID + " not found."))
                .log().all();
    }

    //POST: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testAddTicketWhenSoftwareIdIsWrong() throws JsonProcessingException {
        long softwareID = 100;
        AddTicketRequest request = new AddTicketRequest("New ticket", "Ticket description", 1L, 1L, "1.1", softwareID);
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
                .body(equalTo("Software with ID " + softwareID + " not found."))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket details changed successfully"))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testUpdateTicketWhenCategoryIdIsWrong() throws JsonProcessingException {
        long categoryID = 100;
        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", categoryID, 1L, "1.1", 1L);
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
                .body(equalTo("Category with ID " + categoryID + " not found."))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testUpdateTicketWhenPriorityIdIsWrong() throws JsonProcessingException {
        long priorityID = 100;
        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", 2L, priorityID, "1.1", 1L);
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
                .body(equalTo("Priority with ID " + priorityID + " not found."))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testUpdateTicketWhenSoftwareIdIsWrong() throws JsonProcessingException {
        long softwareID = 100;
        UpdateTicketRequest request = new UpdateTicketRequest(ticketID, "Updated title", "Updated description", 1L, 1L, "1.1", softwareID);
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
                .body(equalTo("Software with ID " + softwareID + " not found."))
                .log().all();
    }

    //PUT: /api/tickets
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testUpdateTicketWhenIdIsWrong() throws JsonProcessingException {
        long ticketID = 100;
        UpdateTicketRequest request = new UpdateTicketRequest(100L, "Updated title", "Updated description", 1L, 1L, "1.1", 1L);
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
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();
    }

    //DELETE: /api/tickets/<ticketID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteTicket() {
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

    //DELETE: /api/tickets/<ticketID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testDeleteTicketWhenIdIsWrong() {
        long ticketID = 100;

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
    public void testAddImageToTicket() {
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

    //POST: /api/tickets/<ticketID>/image
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testAddImageToTicketWhenTicketIdIsWrong() {
        long ticketID = 100;

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
    public void testDeleteImage() {
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

    //DELETE: /api/tickets/image/<imageID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the image ID is incorrect.
    @Test
    public void testDeleteImageWhenIdIsWrong() {
        long imageID = 100;

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply added successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/reply
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testAddTicketReplyWhenTicketIdIsWrong() throws MessagingException, JsonProcessingException {
        long ticketID = 100;
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
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/status
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testChangeTicketStatus() throws MessagingException, JsonProcessingException {
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket status changed successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(1)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/status
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket ID is incorrect.
    @Test
    public void testChangeTicketStatusWhenTicketIdIsWrong() throws MessagingException, JsonProcessingException {
        long ticketID = 100;
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
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket with ID " + ticketID + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/status
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the status ID is incorrect.
    @Test
    public void testChangeTicketStatusWhenStatusIdIsWrong() throws MessagingException, JsonProcessingException {
        long statusID = 100;
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
                .body(equalTo("Status with ID " + statusID + " not found."))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/status
    //Expected status: OK (200)
    //Purpose: Verify the status returned in case of a change to the current status
    @Test
    public void testChangeTicketStatusToCurrentStatus() throws MessagingException, JsonProcessingException {
        long statusID = 1;
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
                .body(equalTo("Ticket status changed successfully"))
                .log().all();

        Mockito.verify(mailService, Mockito.times(0)).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    //DELETE: /api/tickets/reply/<replyID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteTicketReply(){
        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", ticketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket reply removed successfully"))
                .log().all();
    }

    //DELETE: /api/tickets/reply/<replyID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the ticket reply ID is incorrect.
    @Test
    public void testDeleteTicketReplyWhenIdIsWrong() {
        long replyID = 100L;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", replyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Ticket reply with ID " + replyID + " not found."))
                .log().all();
    }
}

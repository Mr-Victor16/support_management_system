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
import org.springframework.transaction.annotation.Transactional;
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
public class TicketControllerUserIntegrationTest {
    @LocalServerPort
    private int port;

    private String jwtToken;
    private Long userTicketID;
    private Long userImageID;
    private Long userTicketReplyID;

    private Long otherTicketID;
    private Long otherImageID;

    private Long closedTicketID;

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
    @Transactional
    public void setUp() throws MessagingException, IOException {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        jwtToken = getJwtToken();

        Mockito.doNothing().when(mailService).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mailService).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());

        ticketRepository.deleteAll();

        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(emptyImage, "png", stream);

        //First ticket - user is the author
        Ticket userticket = new Ticket();
        userticket.setId(1L);
        userticket.setTitle("The website is unreachable");
        userticket.setStatus(statusRepository.getReferenceById(1L));
        userticket.setPriority(priorityRepository.getReferenceById(2L));
        userticket.setSoftware(softwareRepository.getReferenceById(3L));
        userticket.setUser(userRepository.getReferenceById(1L));
        userticket.setDescription("When trying to enter the site, I get an error - This site is unreachable :(");
        userticket.setVersion("1.0");
        userticket.setCategory(categoryRepository.getReferenceById(1L));

        Image image = imageRepository.save(new Image("1.png", stream.toByteArray()));
        List<Image> imageList = new ArrayList<>();
        imageList.add(image);
        userticket.setImages(imageList);

        List<TicketReply> ticketReplyList = new ArrayList<>();
        TicketReply savedReply = ticketReplyRepository.save(new TicketReply(userRepository.getReferenceById(2L),"Have you checked that you have entered the correct address?", LocalDate.of(2021,12,11)));
        ticketReplyList.add(savedReply);
        userticket.setReplies(ticketReplyList);

        Ticket savedTicket = ticketRepository.save(userticket);
        userTicketID = savedTicket.getId();
        userImageID = savedTicket.getImages().get(0).getId();
        userTicketReplyID = savedTicket.getReplies().get(0).getId();

        //Second ticket - user isn't the author
        Ticket secondTicket = new Ticket();
        secondTicket.setId(2L);
        secondTicket.setTitle("Second ticket");
        secondTicket.setStatus(statusRepository.getReferenceById(1L));
        secondTicket.setPriority(priorityRepository.getReferenceById(2L));
        secondTicket.setSoftware(softwareRepository.getReferenceById(3L));
        secondTicket.setUser(userRepository.getReferenceById(2L));
        secondTicket.setDescription("Second ticket description");
        secondTicket.setVersion("1.0");
        secondTicket.setCategory(categoryRepository.getReferenceById(1L));

        Image secondimage = new Image(2L,"1.png", stream.toByteArray());
        imageRepository.save(secondimage);

        List<Image> secondImageList = new ArrayList<>();
        secondImageList.add(secondimage);
        secondTicket.setImages(secondImageList);

        Ticket secondSavedTicket = ticketRepository.save(secondTicket);
        otherTicketID = secondSavedTicket.getId();
        otherImageID = secondSavedTicket.getImages().get(0).getId();

        //Third ticket - user is the author - ticked is closed
        Ticket thirdTicket = new Ticket();
        thirdTicket.setId(3L);
        thirdTicket.setTitle("Third ticket");
        thirdTicket.setStatus(statusRepository.getReferenceById(3L));
        thirdTicket.setPriority(priorityRepository.getReferenceById(3L));
        thirdTicket.setSoftware(softwareRepository.getReferenceById(3L));
        thirdTicket.setUser(userRepository.getReferenceById(1L));
        thirdTicket.setDescription("Third ticket description");
        thirdTicket.setVersion("1.0");
        thirdTicket.setCategory(categoryRepository.getReferenceById(1L));

        Ticket thirdSavedTicket = ticketRepository.save(thirdTicket);
        closedTicketID = thirdSavedTicket.getId();
    }

    private String getJwtToken() throws JsonProcessingException {
        LoginRequest request = new LoginRequest("user", "user");
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
    //Purpose: Verify the returned status if the request contains valid data but the user account doesn't have the required permissions.
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
                .body("size()", equalTo(2))
                .log().all();
    }

    //GET: /api/tickets/user/<userID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the returned status when the user ID is correct, but the user account doesn't have the required permissions.
    @Test
    public void testGetTicketsByUserId() {
        long userID = 1;

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
    public void testGetTicketById() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", userTicketID)
                .when()
                .get("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(userTicketID.intValue()))
                .body("title", notNullValue())
                .body("description", notNullValue())
                .log().all();
    }

    //GET: /api/tickets/<ticketID>
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testGetTicketByIdWhenTicketAuthorIsOtherUser() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", otherTicketID)
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

    //PUT: /api/tickets
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdateTicket() throws JsonProcessingException {
        UpdateTicketRequest request = new UpdateTicketRequest(userTicketID, "Updated title", "Updated description", 2L, 1L, "1.1", 1L);
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
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testUpdateTicketWhenTicketAuthorIsOtherUser() throws JsonProcessingException {
        UpdateTicketRequest request = new UpdateTicketRequest(otherTicketID, "Updated title", "Updated description", 1L, 1L, "1.1", 1L);
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
    public void testDeleteTicket() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", userTicketID)
                .when()
                .delete("/api/tickets/{ticketID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Ticket removed successfully"))
                .log().all();
    }

    //DELETE: /api/tickets/<ticketID>
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testDeleteTicketWhenTicketAuthorIsOtherUser() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("ticketID", otherTicketID)
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
    public void testAddImageToTicket() {
        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", userTicketID)
                .when()
                .post("/api/tickets/{ticketID}/image")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image added successfully"))
                .log().all();
    }

    //POST: /api/tickets/<ticketID>/image
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testAddImageToTicketWhenTicketAuthorIsOtherUser() {
        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "image.png", "dummyImageContent".getBytes(), "image/png")
                .pathParam("ticketID", otherTicketID)
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
    public void testDeleteImage() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", userImageID)
                .when()
                .delete("/api/tickets/image/{imageID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Image removed successfully"))
                .log().all();
    }

    //DELETE: /api/tickets/image/<imageID>
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testDeleteImageWhenTicketAuthorIsOtherUser() {
        given()
                .auth().oauth2(jwtToken)
                .pathParam("imageID", otherImageID)
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
    public void testAddTicketReply() throws MessagingException, JsonProcessingException {
        AddTicketReplyRequest request = new AddTicketReplyRequest(userTicketID, "Reply content");
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

        //Email notification shouldn't be sent because the user is responding in their ticket.
        Mockito.verify(mailService, Mockito.times(0)).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
    }

    //POST: /api/tickets/reply
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when ticket author is other user.
    @Test
    public void testAddTicketReplyWhenTicketAuthorIsOtherUser() throws MessagingException, JsonProcessingException {
        AddTicketReplyRequest request = new AddTicketReplyRequest(otherTicketID, "Reply content");
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
    public void testAddTicketReplyWhenTicketIsClosed() throws MessagingException, JsonProcessingException {
        AddTicketReplyRequest request = new AddTicketReplyRequest(closedTicketID, "Reply content");
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
    public void testChangeTicketStatus() throws MessagingException, JsonProcessingException {
        long statusID = 2;
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(userTicketID, statusID);
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
    public void testDeleteTicketReply(){
        given()
                .auth().oauth2(jwtToken)
                .pathParam("replyID", userTicketReplyID)
                .when()
                .delete("/api/tickets/reply/{replyID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

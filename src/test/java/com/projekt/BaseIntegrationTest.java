package com.projekt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.models.*;
import com.projekt.payload.request.LoginRequest;
import com.projekt.repositories.*;
import com.projekt.services.MailService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest extends SingletonMySQLContainer {
    @LocalServerPort
    protected int port;

    @MockBean
    protected MailService mailService;

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

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    public void setUp() throws MessagingException {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        Mockito.doNothing().when(mailService).sendTicketReplyMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mailService).sendChangeStatusMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    protected String getJwtToken(String login, String password) throws JsonProcessingException {
        LoginRequest request = new LoginRequest(login, password);
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(request);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        return response.jsonPath().getString("token");
    }

    public void clearDatabase(){
        imageRepository.deleteAll();
        ticketReplyRepository.deleteAll();
        ticketRepository.deleteAll();
        knowledgeRepository.deleteAll();
        categoryRepository.deleteAll();
        priorityRepository.deleteAll();
        statusRepository.deleteAll();
        softwareRepository.deleteAll();

        List<User> users = userRepository.findAll();
        if(users.size() > 3){
            List<User> usersToDelete = users.subList(3, users.size());
            usersToDelete.forEach(user -> userRepository.deleteById(user.getId()));
        }
    }

    public List<Software> initializeSoftware(){
        List<Software> softwareList = List.of(
                new Software("Software name", "Software description"),
                new Software("Other software name", "Software description")
        );

        return softwareRepository.saveAll(softwareList);
    }

    public List<Knowledge> initializeKnowledge(Long softwareID){
        List<Knowledge> knowledgeList = List.of(
                new Knowledge("Knowledge name", "First knowledge content", softwareRepository.getReferenceById(softwareID)),
                new Knowledge("Other knowledge name", "Other knowledge content", softwareRepository.getReferenceById(softwareID))
        );

        return knowledgeRepository.saveAll(knowledgeList);
    }

    public Category initializeCategory(String categoryName){
        return categoryRepository.save(new Category(categoryName));
    }

    public List<Category> initializeCategories(){
        initializeCategory("General");
        initializeCategory("Question");
        initializeCategory("Suggestion");

        return categoryRepository.findAll();
    }

    public Priority initializePriority(String priorityName, Integer maxTime){
        return priorityRepository.save(new Priority(priorityName, maxTime));
    }

    public List<Priority> initializePriorities(){
        initializePriority("High", 1);
        initializePriority("Normal", 2);
        initializePriority("Low", 5);

        return priorityRepository.findAll();
    }

    public Status initializeStatus(String statusName, Boolean closeTicket, Boolean defaultStatus){
        return statusRepository.save(new Status(statusName, closeTicket, defaultStatus));
    }

    public List<Status> initializeStatuses(){
        initializeStatus("New",false, true);
        initializeStatus("In progress", false, false);
        initializeStatus("Closed", true, false);

        return statusRepository.findAll();
    }

    public User initializeUser(String username, String password, boolean activated, Role.Types role){
        User user = new User(
                username,
                passwordEncoder.encode(password),
                "username@email.com",
                "Name",
                "Surname",
                activated,
                Set.of(roleRepository.findByType(role))
        );
        return userRepository.save(user);
    }

    public void initializeTicketForUser(Long userID) throws IOException {
        Status status = initializeStatus("New",false, true);
        Priority priority = initializePriority("Normal", 2);
        Category category = initializeCategory("General");
        List<Software> softwareList = initializeSoftware();

        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(emptyImage, "png", stream);

        Ticket ticket = new Ticket();
        ticket.setTitle("The website is unreachable");
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setSoftware(softwareList.get(0));
        ticket.setUser(userRepository.getReferenceById(userID));
        ticket.setDescription("When trying to enter the site, I get an error - This site is unreachable :(");
        ticket.setVersion("1.0");
        ticket.setCategory(category);

        Image image = new Image("1.png", stream.toByteArray());
        List<Image> imageList = new ArrayList<>();
        imageList.add(image);
        ticket.setImages(imageList);

        List<TicketReply> ticketReplyList = new ArrayList<>();
        TicketReply savedReply = ticketReplyRepository.save(
                new TicketReply(
                        userRepository.getReferenceById(userID),
                        "Please, I need help",
                        LocalDate.of(2021,12,11)
                )
        );
        ticketReplyList.add(savedReply);
        ticket.setReplies(ticketReplyList);

        ticketRepository.save(ticket);
    }

    public List<Ticket> initializeTicket(Long softwareID) throws IOException {
        List<Status> statusList = initializeStatuses();
        List<Priority> priorityList = initializePriorities();
        List<Category> categoryList = initializeCategories();

        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(emptyImage, "png", stream);

        //First ticket - user is the author
        Ticket ticket1 = new Ticket();
        ticket1.setTitle("The website is unreachable");
        ticket1.setStatus(statusList.get(0));
        ticket1.setPriority(priorityList.get(1));
        ticket1.setSoftware(softwareRepository.getReferenceById(softwareID));
        ticket1.setUser(userRepository.getReferenceById(1L));
        ticket1.setDescription("When trying to enter the site, I get an error - This site is unreachable :(");
        ticket1.setVersion("1.0");
        ticket1.setCategory(categoryList.get(0));

        Image image1 = new Image("1.png", stream.toByteArray());
        List<Image> imageList1 = new ArrayList<>();
        imageList1.add(image1);
        ticket1.setImages(imageList1);

        List<TicketReply> ticketReplyList = new ArrayList<>();
        TicketReply savedReply = ticketReplyRepository.save(
                new TicketReply(
                        userRepository.getReferenceById(2L),
                        "Have you checked that you have entered the correct address?",

                        LocalDate.of(2021,12,11)
                )
        );
        ticketReplyList.add(savedReply);
        ticket1.setReplies(ticketReplyList);

        //Second ticket - user isn't the author
        Ticket ticket2 = new Ticket();
        ticket2.setTitle("Second ticket");
        ticket2.setStatus(statusList.get(0));
        ticket2.setPriority(priorityList.get(1));
        ticket2.setSoftware(softwareRepository.getReferenceById(softwareID));
        ticket2.setUser(userRepository.getReferenceById(2L));
        ticket2.setDescription("Second ticket description");
        ticket2.setVersion("1.0");
        ticket2.setCategory(categoryList.get(0));

        Image image2 = new Image("2.png", stream.toByteArray());
        List<Image> imageList2 = new ArrayList<>();
        imageList2.add(image2);
        ticket2.setImages(imageList2);

        //Third ticket - user is the author - ticked is closed
        Ticket ticket3 = new Ticket();
        ticket3.setTitle("Third ticket");
        ticket3.setStatus(statusList.get(2));
        ticket3.setPriority(priorityList.get(2));
        ticket3.setSoftware(softwareRepository.getReferenceById(softwareID));
        ticket3.setUser(userRepository.getReferenceById(1L));
        ticket3.setDescription("Third ticket description");
        ticket3.setVersion("1.0");
        ticket3.setCategory(categoryList.get(1));

        List<Ticket> ticketList = List.of(ticket1,ticket2,ticket3);
        return ticketRepository.saveAll(ticketList);
    }
}

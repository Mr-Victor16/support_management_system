package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.repositories.ImageRepository;
import com.projekt.repositories.TicketReplyRepository;
import com.projekt.repositories.TicketRepository;
import com.projekt.repositories.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserControllerOperatorIT extends BaseIntegrationTest {
    String jwtToken;

    @Autowired
    private UserRepository userRepository;

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

    //GET: /api/users/<userID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the user ID is correct.
    @Test
    public void testGetUser() {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", user.getId())
                .when()
                .get("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(user.getId().intValue()))
                .body("username", equalTo(user.getUsername()))
                .body("name", equalTo(user.getName()))
                .body("surname", equalTo(user.getSurname()))
                .body("email", equalTo(user.getEmail()))
                .log().all();
    }

    //GET: /api/users/<userID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the user ID is incorrect.
    @Test
    public void testGetUserWhenIdIsWrong() {
        Long userID = 1000L;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .get("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with ID " + userID + " not found."))
                .log().all();
    }

    //POST: /api/users
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddUser() throws JsonProcessingException {
        AddUserRequest request = new AddUserRequest("username", "password", "newaccount@mail.com", "Name", "Surname", List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String newUserJson = objectMapper.writeValueAsString(request);

        long userNumbers = userRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newUserJson)
                .when()
                .post("/api/users")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("User added"))
                .log().all();

        assertEquals(userRepository.count(), userNumbers+1);
    }

    //POST: /api/users
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if username already used.
    @Test
    public void testAddUserWhenUsernameAlreadyUsed() throws JsonProcessingException {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN);

        AddUserRequest request = new AddUserRequest(user.getUsername(), "password", "newaccount@mail.com", "Name", "Surname", List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String newUserJson = objectMapper.writeValueAsString(request);

        long userNumbers = userRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newUserJson)
                .when()
                .post("/api/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();

        assertEquals(userRepository.count(), userNumbers);
    }

    //POST: /api/users
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if email already used.
    @Test
    public void testAddUserWhenEmailAlreadyUsed() throws JsonProcessingException {
        User user = initializeUser("newAccount", "password", true, Role.Types.ROLE_ADMIN);

        AddUserRequest request = new AddUserRequest("username", "password", user.getEmail(), "Name", "Surname", List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String newUserJson = objectMapper.writeValueAsString(request);

        long userNumbers = userRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newUserJson)
                .when()
                .post("/api/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();

        assertEquals(userRepository.count(), userNumbers);
    }

    //PUT: /api/users
    //Expected status: OK (200)
    //Purpose: Verify the returned status when request data is correct.
    @Test
    public void testUpdateUser() throws JsonProcessingException {
        User user = initializeUser("username", "password",true, Role.Types.ROLE_USER);

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), "NewUsername", "testnew@mail.com", "NewName", "NewSurname", true, List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateUserJson)
                .when()
                .put("/api/users")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("User edited"))
                .log().all();
    }

    //PUT: /api/users
    //Expected status: CONFLICT (409)
    //Purpose: Verify the returned status when username is already used.
    @Test
    public void testUpdateUserWhenUsernameAlreadyUsed() throws JsonProcessingException {
        User user = initializeUser("username", "password",true, Role.Types.ROLE_USER);
        String usedUsername = userRepository.findAll().get(0).getUsername();

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), usedUsername, "testnew@mail.com", "NewName", "NewSurname", true, List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateUserJson)
                .when()
                .put("/api/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();
    }

    //PUT: /api/users
    //Expected status: CONFLICT (409)
    //Purpose: Verify the returned status when email is already used.
    @Test
    public void testUpdateUserWhenEmailAlreadyUsed() throws JsonProcessingException {
        User user = initializeUser("username", "password",true, Role.Types.ROLE_USER);
        String usedEmail = userRepository.findAll().get(0).getEmail();

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), "NewUsername", usedEmail, "NewName", "NewSurname", true, List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateUserJson)
                .when()
                .put("/api/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();
    }

    //PUT: /api/users
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when request data is incorrect.
    @Test
    public void testUpdateUserWhenIdIsWrong() throws JsonProcessingException {
        Long userID = 1000L;

        UpdateUserRequest request = new UpdateUserRequest(userID, "NewUsername", "testnew@mail.com", "NewName", "NewSurname", true, List.of("ROLE_OPERATOR"));
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateUserJson)
                .when()
                .put("/api/users")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with ID " + userID + " not found."))
                .log().all();
    }

    //GET: /api/users
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllUsers() {
        initializeUser("username", "password", true, Role.Types.ROLE_ADMIN);
        int userNumbers = userRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(userNumbers))
                .log().all();
    }

    //DELETE: /api/users/<userID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the user ID is correct.
    @Test
    public void testDeleteUser() {
        Long userID = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN).getId();
        long userNumbers = userRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .delete("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("User removed"))
                .log().all();

        assertEquals(userRepository.count(), userNumbers-1);
    }

    //DELETE: /api/users/<userID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the user ID is incorrect.
    @Test
    public void testDeleteUserWhenIdIsWrong() {
        Long userID = 1000L;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .delete("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with ID " + userID + " not found."))
                .log().all();
    }

    //DELETE: /api/users/<userID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the user ID is correct and user has tickets
    @Test
    public void testDeleteUserWithTickets() throws IOException {
        Long userID = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN).getId();
        initializeTicketForUser(userID);

        long userNumbers = userRepository.findAll().size();
        long ticketNumbers = ticketRepository.findAll().size();
        long ticketReplyNumbers = ticketReplyRepository.findAll().size();
        long imageNumbers = imageRepository.findAll().size();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .delete("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("User removed"))
                .log().all();

        assertEquals(userRepository.count(), userNumbers-1);
        assertEquals(ticketRepository.count(), ticketNumbers-1);
        assertEquals(ticketReplyRepository.count(), ticketReplyNumbers-1);
        assertEquals(imageRepository.count(), imageNumbers-1);
    }

    //DELETE: /api/users/<userID>
    //Expected status: FORBIDDEN (403)
    //Purpose: Verify the returned status when user is last one with role Admin
    @Test
    public void testDeleteUserWhenLastOneAdmin() {
        long userNumbers = userRepository.findAll().size();
        Long adminID = userRepository.findAll().get(2).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", adminID)
                .when()
                .delete("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("Default administrator account cannot be deleted."))
                .log().all();

        assertEquals(userRepository.count(), userNumbers);
    }
}

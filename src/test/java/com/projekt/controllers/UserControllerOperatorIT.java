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

    /**
     * Controller method: UserController.getUserById
     * HTTP Method: GET
     * Endpoint: /api/users/{userID}
     * Expected Status: 200 OK
     * Scenario: Successfully retrieve user details by ID.
     * Verification: Confirms the response contains correct user details.
     */
    @Test
    public void getUserById_ReturnsUserDetailsSuccessfully() {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);

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

    /**
     * Controller method: UserController.getUserById
     * HTTP Method: GET
     * Endpoint: /api/users/{userID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempt to retrieve user details using a non-existent ID.
     */
    @Test
    public void getUserById_InvalidId_ReturnsNotFound() {
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

    /**
     * Controller method: UserController.addUser
     * HTTP Method: POST
     * Endpoint: /api/users
     * Expected Status: 200 OK
     * Scenario: Successfully add a new user with valid data.
     * Verification: Confirms the user count increases.
     */
    @Test
    public void addUser_ValidData_ReturnsSuccess() throws JsonProcessingException {
        AddUserRequest request = new AddUserRequest("username", "password", "newaccount@mail.com", "Name", "Surname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.addUser
     * HTTP Method: POST
     * Endpoint: /api/users
     * Expected Status: 409 CONFLICT
     * Scenario: Attempt to create a user with an already used username.
     * Verification: Confirms the user count remains unchanged.
     */
    @Test
    public void addUser_DuplicateUsername_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_ADMIN);

        AddUserRequest request = new AddUserRequest(user.getUsername(), "password", "newaccount@mail.com", "Name", "Surname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.addUser
     * HTTP Method: POST
     * Endpoint: /api/users
     * Expected Status: 409 CONFLICT
     * Scenario: Attempt to create a user with an already used e-mail address.
     * Verification: Confirms the user count remains unchanged.
     */
    @Test
    public void addUser_DuplicateEmail_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("newAccount", "password", Role.Types.ROLE_ADMIN);

        AddUserRequest request = new AddUserRequest("username", "password", user.getEmail(), "Name", "Surname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.updateUser
     * HTTP Method: PUT
     * Endpoint: /api/users
     * Expected Status: 200 OK
     * Scenario: Successfully update user details with valid data.
     */
    @Test
    public void updateUser_ValidData_ReturnsSuccess() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), "NewUsername", "testnew@mail.com", "NewName", "NewSurname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.updateUser
     * HTTP Method: PUT
     * Endpoint: /api/users
     * Expected Status: 409 CONFLICT
     * Scenario: Update user to an already used username.
     */
    @Test
    public void updateUser_UsernameAlreadyUsed_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);
        String usedUsername = userRepository.findAll().get(0).getUsername();

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), usedUsername, "testnew@mail.com", "NewName", "NewSurname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.updateUser
     * HTTP Method: PUT
     * Endpoint: /api/users
     * Expected Status: 409 CONFLICT
     * Scenario: Update user to an already used e-mail address.
     */
    @Test
    public void updateUser_EmailAlreadyUsed_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);
        String usedEmail = userRepository.findAll().get(0).getEmail();

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), "NewUsername", usedEmail, "NewName", "NewSurname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.updateUser
     * HTTP Method: PUT
     * Endpoint: /api/users
     * Expected Status: 404 NOT FOUND
     * Scenario: Updating a user with an incorrect ID.
     */
    @Test
    public void updateUser_InvalidUserId_ReturnsNotFound() throws JsonProcessingException {
        Long userID = 1000L;

        UpdateUserRequest request = new UpdateUserRequest(userID, "NewUsername", "testnew@mail.com", "NewName", "NewSurname", "ROLE_OPERATOR");
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

    /**
     * Controller method: UserController.getAllUsers
     * HTTP Method: GET
     * Endpoint: /api/users
     * Expected Status: 200 OK
     * Scenario: Retrieving all users from the repository.
     * Verification: Confirms the returned list size matches the expected user count.
     */
    @Test
    public void getAllUsers_ReturnsUserListSuccessfully() {
        initializeUser("username", "password", Role.Types.ROLE_ADMIN);
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

    /**
     * Controller method: UserController.deleteUser
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userID}
     * Expected Status: 200 OK
     * Scenario: Deleting a user with a valid user ID.
     * Verification: Confirms the user count decreases.
     */
    @Test
    public void deleteUser_ValidUserId_ReturnsSuccess() {
        Long userID = initializeUser("username", "password", Role.Types.ROLE_ADMIN).getId();
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

    /**
     * Controller method: UserController.deleteUser
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Deleting a user with non-existing user ID.
     */
    @Test
    public void deleteUser_InvalidUserId_ReturnsNotFound() {
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

    /**
     * Controller method: UserController.deleteUser
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userID}
     * Expected Status: 200 OK
     * Scenario: Deleting a user with a valid user ID when the user has associated tickets.
     * Verification: Confirms that related data is also deleted.
     */
    @Test
    public void deleteUser_UserWithTickets_ReturnsSuccess() throws IOException {
        Long userID = initializeUser("username", "password", Role.Types.ROLE_ADMIN).getId();
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

    /**
     * Controller method: UserController.deleteUser
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userID}
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to delete the last user with the admin role.
     * Verification: Confirms the user count remains unchanged.
     */
    @Test
    public void deleteUser_LastAdminAccount_ReturnsForbidden() {
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

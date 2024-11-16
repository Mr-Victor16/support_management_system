package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.LoginRequest;
import com.projekt.payload.request.RegisterRequest;
import com.projekt.repositories.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        clearDatabase();
    }

    /**
     * Controller method: AuthController.authenticateUser
     * HTTP Method: POST
     * Endpoint: /api/auth/login
     * Expected Status: 200 OK
     * Scenario: Authenticating a user with valid credentials.
     * Verification: Confirms returned user profile data (id, username, name, surname, email).
     */
    @Test
    public void authenticate_ValidCredentials_ReturnsUserDetailsSuccessfully() throws JsonProcessingException {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_USER);

        LoginRequest request = new LoginRequest(user.getUsername(), "password");
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(request);

        given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/api/auth/login")
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
     * Controller method: AuthController.authenticateUser
     * HTTP Method: POST
     * Endpoint: /api/auth/login
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to authenticate with a non-existent username.
     */
    @Test
    public void authenticate_NonExistentUsername_ReturnsNotFound() throws JsonProcessingException {
        LoginRequest request = new LoginRequest("noUser", "password");
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(request);

        given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("User with name '" + request.username() + "' not found."))
                .log().all();
    }

    /**
     * Controller method: AuthController.authenticateUser
     * HTTP Method: POST
     * Endpoint: /api/auth/login
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to authenticate with an incorrect password.
     */
    @Test
    public void authenticate_IncorrectPassword_ReturnsUnauthorized() throws JsonProcessingException {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_USER);

        LoginRequest request = new LoginRequest(user.getUsername(), "wrongPassword");
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(request);

        given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Bad credentials"))
                .log().all();
    }

    /**
     * Controller method: AuthController.registerUser
     * HTTP Method: POST
     * Endpoint: /api/auth/register
     * Expected Status: 200 OK
     * Scenario: Registering a new user with unique username and email.
     * Verification: Verifies an increment in the user count in the repository.
     */
    @Test
    public void registerUser_UniqueData_ReturnsSuccess() throws JsonProcessingException {
        RegisterRequest request = new RegisterRequest("newUser", "password", "newUserEmail@email.com", "Name", "Surname");
        ObjectMapper objectMapper = new ObjectMapper();
        String registerJson = objectMapper.writeValueAsString(request);

        int numberUsers = userRepository.findAll().size();

        given()
                .contentType(ContentType.JSON)
                .body(registerJson)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("User added"))
                .log().all();

        assertEquals((numberUsers+1), userRepository.findAll().size());
    }

    /**
     * Controller method: AuthController.registerUser
     * HTTP Method: POST
     * Endpoint: /api/auth/register
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to register a new user with an already existing username.
     * Verification: Verifies no change in the user count in the repository.
     */
    @Test
    public void registerUser_DuplicateUsername_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_USER);

        RegisterRequest request = new RegisterRequest(user.getUsername(), "password", "newUserEmail@email.com", "Name", "Surname");
        ObjectMapper objectMapper = new ObjectMapper();
        String registerJson = objectMapper.writeValueAsString(request);

        int numberUsers = userRepository.findAll().size();

        given()
                .contentType(ContentType.JSON)
                .body(registerJson)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();

        assertEquals((numberUsers), userRepository.findAll().size());
    }

    /**
     * Controller method: AuthController.registerUser
     * HTTP Method: POST
     * Endpoint: /api/auth/register
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to register a new user with an already existing email address.
     * Verification: Verifies no change in the user count in the repository.
     */
    @Test
    public void registerUser_DuplicateEmail_ReturnsConflict() throws JsonProcessingException {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_USER);

        RegisterRequest request = new RegisterRequest("newUser", "password", user.getEmail(), "Name", "Surname");
        ObjectMapper objectMapper = new ObjectMapper();
        String registerJson = objectMapper.writeValueAsString(request);

        int numberUsers = userRepository.findAll().size();

        given()
                .contentType(ContentType.JSON)
                .body(registerJson)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Username '" + request.username() + "' or Email '" + request.email() + "' already exists."))
                .log().all();

        assertEquals((numberUsers), userRepository.findAll().size());
    }
}

package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserControllerUserIT extends BaseIntegrationTest {
    String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    /**
     * Controller method: UserController.getUser
     * HTTP Method: GET
     * Endpoint: /api/users/{userID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to access user account information as a user without sufficient permissions.
     */
    @Test
    public void getUser_InsufficientPermissions_ReturnsUnauthorized() {
        User user = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", user.getId())
                .when()
                .get("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: UserController.addUser
     * HTTP Method: POST
     * Endpoint: /api/users
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add user account as a user without sufficient permissions.
     */
    @Test
    public void addUser_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        AddUserRequest request = new AddUserRequest("username", "password", "newaccount@mail.com", "Name", "Surname", "ROLE_OPERATOR");
        ObjectMapper objectMapper = new ObjectMapper();
        String newUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newUserJson)
                .when()
                .post("/api/users")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: UserController.updateUser
     * HTTP Method: PUT
     * Endpoint: /api/users
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update user account as a user without sufficient permissions.
     */
    @Test
    public void updateUser_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        User user = initializeUser("username", "password",true, Role.Types.ROLE_USER);

        UpdateUserRequest request = new UpdateUserRequest(user.getId(), "NewUsername", "testnew@mail.com", "NewName", "NewSurname", true, "ROLE.NOT_EXIST");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateUserJson)
                .when()
                .put("/api/users")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: UserController.getAllUsers
     * HTTP Method: GET
     * Endpoint: /api/users
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to access users list as a user without sufficient permissions.
     */
    @Test
    public void getAllUsers_InsufficientPermissions_ReturnsUnauthorized() {
        initializeUser("username", "password", true, Role.Types.ROLE_ADMIN);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: UserController.deleteUser
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to delete user account as a user without sufficient permissions.
     */
    @Test
    public void deleteUser_InsufficientPermissions_ReturnsUnauthorized() {
        Long userID = initializeUser("username", "password", true, Role.Types.ROLE_ADMIN).getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("userID", userID)
                .when()
                .delete("/api/users/{userID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

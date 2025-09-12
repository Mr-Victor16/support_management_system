package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.update.UpdateProfileDetailsRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ProfileControllerIT extends BaseIntegrationTest {
    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        clearDatabase();
    }

    /**
     * Controller method: ProfileController.getProfile
     * HTTP Method: GET
     * Endpoint: /api/profiles
     * Expected status: 200 OK
     * Scenario: Retrieving own user profile data as ADMIN.
     * Verification: Confirms returned profile data fields match the authenticated user's details.
     */
    @Test
    public void getProfile_AdminRoleAndValidToken_ReturnsUserProfileDataSuccessfully() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_ADMIN);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/profiles")
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
     * Controller method: ProfileController.getProfile
     * HTTP Method: GET
     * Endpoint: /api/profiles
     * Expected status: 200 OK
     * Scenario: Retrieving own user profile data as OPERATOR.
     * Verification: Confirms returned profile data fields match the authenticated user's details.
     */
    @Test
    public void getProfile_OperatorRoleAndValidToken_ReturnsUserProfileDataSuccessfully() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_OPERATOR);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/profiles")
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
     * Controller method: ProfileController.getProfile
     * HTTP Method: GET
     * Endpoint: /api/profiles
     * Expected status: 200 OK
     * Scenario: Retrieving own user profile data as USER.
     * Verification: Confirms returned profile data fields match the authenticated user's details.
     */
    @Test
    public void getProfile_UserRoleAndValidToken_ReturnsUserProfileDataSuccessfully() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/profiles")
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
     * Controller method: ProfileController.updateProfile
     * HTTP Method: PUT
     * Endpoint: /api/profiles
     * Expected Status: 200 OK
     * Scenario: Updating own user profile data as ADMIN.
     */
    @Test
    public void updateProfile_AdminRoleAndValidData_ReturnsSuccess() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_ADMIN);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        UpdateProfileDetailsRequest request = new UpdateProfileDetailsRequest("NewName", "NewSurname", "NewPassword");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateProfileJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateProfileJson)
                .when()
                .put("/api/profiles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Profile updated"))
                .log().all();
    }

    /**
     * Controller method: ProfileController.updateProfile
     * HTTP Method: PUT
     * Endpoint: /api/profiles
     * Expected Status: 200 OK
     * Scenario: Updating own user profile data as OPERATOR.
     */
    @Test
    public void updateProfile_OperatorRoleAndValidData_ReturnsSuccess() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_OPERATOR);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        UpdateProfileDetailsRequest request = new UpdateProfileDetailsRequest("NewName", "NewSurname", "NewPassword");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateProfileJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateProfileJson)
                .when()
                .put("/api/profiles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Profile updated"))
                .log().all();
    }

    /**
     * Controller method: ProfileController.updateProfile
     * HTTP Method: PUT
     * Endpoint: /api/profiles
     * Expected Status: 200 OK
     * Scenario: Updating own user profile data as USER.
     */
    @Test
    public void updateProfile_UserRoleAndValidData_ReturnsSuccess() throws JsonProcessingException {
        User user = initializeUser("username", "password", Role.Types.ROLE_USER);
        String jwtToken = getJwtToken(user.getUsername(), "password");

        UpdateProfileDetailsRequest request = new UpdateProfileDetailsRequest("NewName", "NewSurname", "NewPassword");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateProfileJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateProfileJson)
                .when()
                .put("/api/profiles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Profile updated"))
                .log().all();
    }
}

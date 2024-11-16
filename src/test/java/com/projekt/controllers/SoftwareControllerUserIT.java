package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Software;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class SoftwareControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    /**
     * Controller method: SoftwareController.getAllSoftware
     * HTTP Method: GET
     * Endpoint: /api/software
     * Expected Status: 200 OK
     * Scenario: Retrieving all software.
     * Verification: Confirms the returned list size matches the expected software count in the repository.
     */
    @Test
    public void getAllSoftware_ReturnsSoftwareListSuccessfully() {
        List<Software> softwareList = initializeSoftware();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/software")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(softwareList.size()))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.getAllSoftwareWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/software/use
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Verifying that the user role cannot access the software use numbers.
     */
    @Test
    public void getAllSoftwareWithUseNumbers_ReturnsUnauthorized() {
        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/software/use")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.getSoftwareById
     * HTTP Method: GET
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 200 OK
     * Scenario: Retrieving a software by ID.
     * Verification: Confirms that the software details returned match the expected values.
     */
    @Test
    public void getSoftwareById_ReturnsSoftwareSuccessfully() {
        Software software = initializeSingleSoftware("Software name", "Software description");

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", software.getId())
                .when()
                .get("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(software.getId().intValue()))
                .body("name", equalTo(software.getName()))
                .body("description", equalTo(software.getDescription()))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.getSoftwareById
     * HTTP Method: GET
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Retrieving a software by incorrect ID.
     */
    @Test
    public void getSoftwareById_InvalidId_ReturnsNotFound() {
        long softwareID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", softwareID)
                .when()
                .get("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + softwareID + " not found."))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.updateSoftware
     * HTTP Method: PUT
     * Endpoint: /api/software
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempt to update software with insufficient permissions.
     */
    @Test
    public void updateSoftware_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        Software software = initializeSingleSoftware("Software name", "Software description");

        UpdateSoftwareRequest request = new UpdateSoftwareRequest(software.getId(), "Updated title", "Updated description");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateSoftwareJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateSoftwareJson)
                .when()
                .put("/api/software")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.addSoftware
     * HTTP Method: POST
     * Endpoint: /api/software
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempt to add software with insufficient permissions.
     */
    @Test
    public void addSoftware_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        AddSoftwareRequest request = new AddSoftwareRequest("New software", "New software description");
        ObjectMapper objectMapper = new ObjectMapper();
        String newSoftwareJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newSoftwareJson)
                .when()
                .post("/api/software")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.deleteSoftware
     * HTTP Method: DELETE
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempt to delete software with insufficient permissions.
     */
    @Test
    public void deleteSoftware_InsufficientPermissions_ReturnsUnauthorized() {
        Software software = initializeSingleSoftware("Software name", "Software description");

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", software.getId())
                .when()
                .delete("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

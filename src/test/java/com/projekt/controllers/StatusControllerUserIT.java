package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class StatusControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    /**
     * Controller method: StatusController.getAllStatuses
     * HTTP Method: GET
     * Endpoint: /api/statuses
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Verifying that the user role cannot access the statuses list.
     */
    @Test
    public void getAllStatuses_InsufficientPermissions_ReturnsUnauthorized() {
        initializeStatuses();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/statuses")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: StatusController.getAllStatusesWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/statuses/use
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Verifying that the user role cannot access the statuses use numbers.
     */
    @Test
    public void getAllStatusesWithUseNumbers_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/statuses/use")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: StatusController.getStatusById
     * HTTP Method: GET
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve a status by ID as a user without sufficient permissions
     */
    @Test
    public void getStatusById_InsufficientPermissions_ReturnsUnauthorized() {
        Status status = initializeStatus("Closed", true, true);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", status.getId())
                .when()
                .get("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: StatusController.updateStatus
     * HTTP Method: PUT
     * Endpoint: /api/statuses
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update a status as a user without sufficient permissions.
     */
    @Test
    public void updateStatus_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        Status status = initializeStatus("Closed", false, false);

        UpdateStatusRequest request = new UpdateStatusRequest(status.getId(), "Updated status", true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateStatusJson)
                .when()
                .put("/api/statuses")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: StatusController.addStatus
     * HTTP Method: POST
     * Endpoint: /api/statuses
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add a status as a user without sufficient permissions.
     */
    @Test
    public void addStatus_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        AddStatusRequest request = new AddStatusRequest("New status", true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String addStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addStatusJson)
                .when()
                .post("/api/statuses")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: StatusController.deleteStatus
     * HTTP Method: DELETE
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to delete a status as a user without sufficient permissions.
     */
    @Test
    public void deleteStatus_InsufficientPermissions_ReturnsUnauthorized() {
        Status status = initializeStatus("Closed", true, false);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", status.getId())
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

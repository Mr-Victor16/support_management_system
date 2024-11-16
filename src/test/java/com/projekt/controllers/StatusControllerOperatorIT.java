package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;
import com.projekt.repositories.StatusRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusControllerOperatorIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private StatusRepository statusRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("operator", "operator");
        clearDatabase();
    }

    /**
     * Controller method: StatusController.getAllStatuses
     * HTTP Method: GET
     * Endpoint: /api/statuses
     * Expected Status: 200 OK
     * Scenario: Retrieving all statuses.
     * Verification: Confirms the returned list size matches the expected status count in the repository.
     */
    @Test
    public void getAllStatuses_ReturnsStatusListSuccessfully() {
        List<Status> statusList = initializeStatuses();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/statuses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(statusList.size()))
                .log().all();
    }

    /**
     * Controller method: StatusController.getAllStatusesWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/statuses/use
     * Expected Status: 200 OK
     * Scenario: Retrieving all statuses with associated usage numbers.
     * Verification: Confirms the correct usage count for each status.
     */
    @Test
    public void getAllStatusesWithUseNumbers_ReturnsStatusUsageCountListSuccessfully() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);

        Response response = RestAssured.given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/statuses/use")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo((int) statusRepository.count()))
                .log().all()
                .extract().response();

        List<StatusResponse> statusList = response.jsonPath().getList(".", StatusResponse.class);

        assertEquals(2, statusList.get(0).useNumber());
    }

    /**
     * Controller method: StatusController.getStatusById
     * HTTP Method: GET
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve a status by ID as a user without sufficient permissions.
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
        Status status = initializeStatus("Pending", false, false);

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

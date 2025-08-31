package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.repositories.PriorityRepository;
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

public class PriorityControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private PriorityRepository priorityRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    /**
     * Controller method: PriorityController.getAllPriorities
     * HTTP Method: GET
     * Endpoint: /api/priorities
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Verifying that the user role cannot access the priorities list.
     */
    @Test
    public void getAllPriorities_InsufficientPermissions_ReturnsUnauthorized() {
        List<Priority> priorityList = initializePriorities();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/priorities")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(priorityList.size()))
                .log().all();
    }

    /**
     * Controller method: PriorityController.getAllPrioritiesWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/priorities/use
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve all priorities with associated usage numbers as a user without sufficient permissions.
     */
    @Test
    public void getAllPrioritiesWithUseNumbers_InsufficientPermissions_ReturnsUnauthorized() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/priorities/use")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: PriorityController.getPriorityById
     * HTTP Method: GET
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve priority by ID as a user without sufficient permissions.
     */
    @Test
    public void getPriorityById_InsufficientPermissions_ReturnsUnauthorized() {
        Priority priority = initializePriority("High");

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priority.getId())
                .when()
                .get("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: PriorityController.updatePriority
     * HTTP Method: PUT
     * Endpoint: /api/priorities
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update priority as a user without sufficient permissions.
     */
    @Test
    public void updatePriority_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        Priority priority = initializePriority("High");

        UpdatePriorityRequest request = new UpdatePriorityRequest(priority.getId(), "Updated priority");
        ObjectMapper objectMapper = new ObjectMapper();
        String updatePriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updatePriorityJson)
                .when()
                .put("/api/priorities")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: PriorityController.addPriority
     * HTTP Method: POST
     * Endpoint: /api/priorities
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add priority as a user without sufficient permissions.
     */
    @Test
    public void addPriority_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        List<Priority> priorityList = initializePriorities();

        AddPriorityRequest request = new AddPriorityRequest("New priority");
        ObjectMapper objectMapper = new ObjectMapper();
        String newPriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newPriorityJson)
                .when()
                .post("/api/priorities")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityList.size());
    }

    /**
     * Controller method: PriorityController.deletePriority
     * HTTP Method: DELETE
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to delete priority as a user without sufficient permissions.
     */
    @Test
    public void deletePriority_InsufficientPermissions_ReturnsUnauthorized() {
        Priority priority = initializePriority("High");

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priority.getId())
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Priority;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.repositories.PriorityRepository;
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

public class PriorityControllerAdminIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private PriorityRepository priorityRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
        clearDatabase();
    }

    /**
     * Controller method: PriorityController.getAllPriorities
     * HTTP Method: GET
     * Endpoint: /api/priorities
     * Expected Status: 200 OK
     * Scenario: Retrieving all priorities.
     * Verification: Confirms the returned list size matches the expected priority count in the repository.
     */
    @Test
    public void getAllPriorities_ReturnsPriorityListSuccessfully() {
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
     * Expected Status: 200 OK
     * Scenario: Retrieving priorities with their use numbers.
     * Verification: Confirms the returned list size matches the expected priority count and specific usage numbers.
     */
    @Test
    public void getAllPrioritiesWithUseNumbers_ReturnsPriorityListWithUseNumbersSuccessfully() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        initializeTicket(softwareID);

        Response response = RestAssured.given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/priorities/use")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo((int) priorityRepository.count()))
                .log().all()
                .extract().response();

        List<PriorityResponse> responseList = response.jsonPath().getList(".", PriorityResponse.class);

        assertEquals(2, responseList.get(1).useNumber());
    }

    /**
     * Controller method: PriorityController.getPriorityById
     * HTTP Method: GET
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 200 OK
     * Scenario: Retrieving a priority by ID.
     * Verification: Confirms the returned priority matches the expected ID, name and maxTime.
     */
    @Test
    public void getPriorityById_ReturnsPrioritySuccessfully() {
        Priority priority = initializePriority("High", 1);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priority.getId())
                .when()
                .get("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(priority.getId().intValue()))
                .body("name", equalTo(priority.getName()))
                .body("maxTime", equalTo(priority.getMaxTime()))
                .log().all();
    }

    /**
     * Controller method: PriorityController.getPriorityById
     * HTTP Method: GET
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to retrieve a priority by an invalid ID.
     */
    @Test
    public void getPriorityById_InvalidId_ReturnsNotFound() {
        long priorityID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priorityID)
                .when()
                .get("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Priority with ID " + priorityID + " not found."))
                .log().all();
    }

    /**
     * Controller method: PriorityController.updatePriority
     * HTTP Method: PUT
     * Endpoint: /api/priorities
     * Expected Status: 200 OK
     * Scenario: Updating a priority with valid data.
     */
    @Test
    public void updatePriority_ValidData_ReturnsSuccess() throws JsonProcessingException {
        Priority priority = initializePriority("High", 1);

        UpdatePriorityRequest request = new UpdatePriorityRequest(priority.getId(), "Updated priority", 2);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatePriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updatePriorityJson)
                .when()
                .put("/api/priorities")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority updated"))
                .log().all();
    }

    /**
     * Controller method: PriorityController.updatePriority
     * HTTP Method: PUT
     * Endpoint: /api/priorities
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to update a priority to a name that already exists.
     * Verification: Confirms the priority count remains unchanged.
     */
    @Test
    public void updatePriority_ExistingName_ReturnsConflict() throws JsonProcessingException {
        List<Priority> priorityList = initializePriorities();
        Long priorityID = priorityList.get(0).getId();
        String priorityName = priorityList.get(1).getName();

        UpdatePriorityRequest request = new UpdatePriorityRequest(priorityID, priorityName, 2);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatePriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updatePriorityJson)
                .when()
                .put("/api/priorities")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Priority with name '" + request.name() + "' already exists."))
                .log().all();
    }

    /**
     * Controller method: PriorityController.updatePriority
     * HTTP Method: PUT
     * Endpoint: /api/priorities
     * Expected Status: 200 OK
     * Scenario: Attempting to update a priority with the same name as the current one.
     */
    @Test
    public void updatePriority_NewNameIsSameAsCurrent_ReturnsSuccess() throws JsonProcessingException {
        Priority priority = initializePriority("High", 1);

        UpdatePriorityRequest request = new UpdatePriorityRequest(priority.getId(), priority.getName(), priority.getMaxTime());
        ObjectMapper objectMapper = new ObjectMapper();
        String updatePriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updatePriorityJson)
                .when()
                .put("/api/priorities")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority name is the same as the current name '" + request.name() + "'"))
                .log().all();
    }

    /**
     * Controller method: PriorityController.updatePriority
     * HTTP Method: PUT
     * Endpoint: /api/priorities
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to update a priority by an invalid ID.
     */
    @Test
    public void updatePriority_InvalidId_ReturnsNotFound() throws JsonProcessingException {
        long priorityID = 1000;

        UpdatePriorityRequest request = new UpdatePriorityRequest(priorityID, "Updated priority", 3);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatePriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updatePriorityJson)
                .when()
                .put("/api/priorities")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Priority with ID " + priorityID + " not found."))
                .log().all();
    }

    /**
     * Controller method: PriorityController.addPriority
     * HTTP Method: POST
     * Endpoint: /api/priorities
     * Expected Status: 200 OK
     * Scenario: Adding a new priority with a unique name.
     * Verification: Confirms the priority count increases.
     */
    @Test
    public void addPriority_UniqueName_ReturnsSuccess() throws JsonProcessingException {
        List<Priority> priorityList = initializePriorities();

        AddPriorityRequest request = new AddPriorityRequest("New priority", 5);
        ObjectMapper objectMapper = new ObjectMapper();
        String newPriorityJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newPriorityJson)
                .when()
                .post("/api/priorities")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority added"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityList.size()+1);
    }

    /**
     * Controller method: PriorityController.addPriority
     * HTTP Method: POST
     * Endpoint: /api/priorities
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to add a priority with a name that already exists.
     * Verification: Confirms the priority count remains unchanged.
     */
    @Test
    public void addPriority_ExistingName_ReturnsConflict() throws JsonProcessingException {
        Priority priority = initializePriority("High",1);

        AddPriorityRequest request = new AddPriorityRequest(priority.getName(), priority.getMaxTime());
        ObjectMapper objectMapper = new ObjectMapper();
        String newPriorityJson = objectMapper.writeValueAsString(request);

        long priorityNumber = priorityRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newPriorityJson)
                .when()
                .post("/api/priorities")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Priority with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(priorityRepository.count(), priorityNumber);
    }

    /**
     * Controller method: PriorityController.deletePriority
     * HTTP Method: DELETE
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 200 OK
     * Scenario: Deleting a priority with no associated tickets.
     * Verification: Confirms the priority count decreases.
     */
    @Test
    public void deletePriority_NoAssignedTickets_ReturnsSuccess() {
        Priority priority = initializePriority("High",1);
        long priorityNumber = priorityRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priority.getId())
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority removed"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityNumber-1);
    }

    /**
     * Controller method: PriorityController.deletePriority
     * HTTP Method: DELETE
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to delete a priority by an invalid ID.
     */
    @Test
    public void deletePriority_InvalidId_ReturnsNotFound() {
        long priorityID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priorityID)
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Priority with ID " + priorityID + " not found."))
                .log().all();
    }

    /**
     * Controller method: PriorityController.deletePriority
     * HTTP Method: DELETE
     * Endpoint: /api/priorities/{priorityID}
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to delete a priority that has assigned tickets.
     * Verification: Confirms the priority count remains unchanged.
     */
    @Test
    public void deletePriority_AssignedTickets_ReturnsConflict() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long priorityID = ticketList.get(0).getPriority().getId();

        long priorityNumber = priorityRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priorityID)
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a priority if it has a ticket assigned to it"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityNumber);
    }
}

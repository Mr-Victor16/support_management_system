package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Status;
import com.projekt.models.Ticket;
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

public class StatusControllerAdminIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private StatusRepository statusRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
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
     * Expected Status: 200 OK
     * Scenario: Retrieving a specific status by its ID.
     * Verification: Confirms the status details match the expected values.
     */
    @Test
    public void getStatusById_ReturnsStatusSuccessfully() {
        Status status = initializeStatus("Closed", true, true);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", status.getId())
                .when()
                .get("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(status.getId().intValue()))
                .body("name", equalTo(status.getName()))
                .body("closeTicket", equalTo(status.isCloseTicket()))
                .body("defaultStatus", equalTo(status.isDefaultStatus()))
                .log().all();
    }

    /**
     * Controller method: StatusController.getStatusById
     * HTTP Method: GET
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to retrieve a status by an invalid ID.
     */
    @Test
    public void getStatusById_InvalidId_ReturnsNotFound() {
        long statusID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", statusID)
                .when()
                .get("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Status with ID " + statusID + " not found."))
                .log().all();
    }

    /**
     * Controller method: StatusController.updateStatus
     * HTTP Method: PUT
     * Endpoint: /api/statuses
     * Expected Status: 200 OK
     * Scenario: Updating a status with valid data.
     */
    @Test
    public void updateStatus_ReturnsSuccess() throws JsonProcessingException {
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Status details updated"))
                .log().all();
    }

    /**
     * Controller method: StatusController.updateStatus
     * HTTP Method: PUT
     * Endpoint: /api/statuses
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to update a status by an invalid ID.
     */
    @Test
    public void updateStatus_InvalidId_ReturnsNotFound() throws JsonProcessingException {
        long statusID = 1000;

        UpdateStatusRequest request = new UpdateStatusRequest(statusID, "Updated status", true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateStatusJson)
                .when()
                .put("/api/statuses")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Status with ID " + statusID + " not found."))
                .log().all();
    }

    /**
     * Controller method: StatusController.updateStatus
     * HTTP Method: PUT
     * Endpoint: /api/statuses
     * Expected Status: 200 OK
     * Scenario: Attempting to update a status with the same name as the current one.
     */
    @Test
    public void updateStatus_NewNameIsSameAsCurrent_ReturnsSuccess() throws JsonProcessingException {
        Status status = initializeStatus("Pending", false, false);

        UpdateStatusRequest request = new UpdateStatusRequest(status.getId(), status.getName(), true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateStatusJson)
                .when()
                .put("/api/statuses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Status name is the same as the current name '" + request.name() + "'"))
                .log().all();
    }

    /**
     * Controller method: StatusController.updateStatus
     * HTTP Method: PUT
     * Endpoint: /api/statuses
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to update a status with a name that already exists.
     * Verification: Confirms the status count remains unchanged.
     */
    @Test
    public void updateStatus_NewNameIsAlreadyUsed_ReturnsConflict() throws JsonProcessingException {
        List<Status> statusList = initializeStatuses();
        Long statusID = statusList.get(0).getId();
        String statusName = statusList.get(1).getName();

        long statusNumber = statusRepository.count();

        UpdateStatusRequest request = new UpdateStatusRequest(statusID, statusName, true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateStatusJson)
                .when()
                .put("/api/statuses")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Status with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(statusRepository.count(), statusNumber);
    }

    /**
     * Controller method: StatusController.addStatus
     * HTTP Method: POST
     * Endpoint: /api/statuses
     * Expected Status: 200 OK
     * Scenario: Adding a new status with a unique name.
     * Verification: Confirms the status count increases.
     */
    @Test
    public void addStatus_UniqueName_ReturnsSuccess() throws JsonProcessingException {
        List<Status> statusList = initializeStatuses();

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Status added"))
                .log().all();

        assertEquals(statusRepository.count(), statusList.size()+1);
    }

    /**
     * Controller method: StatusController.addStatus
     * HTTP Method: POST
     * Endpoint: /api/statuses
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to add a status with an already existing name.
     * Verification: Confirms the status count remains unchanged.
     */
    @Test
    public void addStatus_NameAlreadyExists_ReturnsConflict() throws JsonProcessingException {
        Status status = initializeStatus("Pending", false, false);
        long statusNumber = statusRepository.count();

        AddStatusRequest request = new AddStatusRequest(status.getName(), true, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String addStatusJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addStatusJson)
                .when()
                .post("/api/statuses")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Status with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(statusRepository.count(), statusNumber);
    }

    /**
     * Controller method: StatusController.deleteStatus
     * HTTP Method: DELETE
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 200 OK
     * Scenario: Deleting a status with no associated tickets.
     * Verification: Confirms the status count decreases.
     */
    @Test
    public void deleteStatus_NoAssignedTickets_ReturnsSuccess() {
        List<Status> statusList = initializeStatuses();
        Status status = statusList.get(2);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", status.getId())
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Status removed"))
                .log().all();

        assertEquals(statusRepository.count(), statusList.size()-1);
    }

    /**
     * Controller method: StatusController.deleteStatus
     * HTTP Method: DELETE
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to delete a status by an invalid ID.
     */
    @Test
    public void deleteStatus_InvalidId_ReturnsNotFound() {
        long statusID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", statusID)
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Status with ID " + statusID + " not found."))
                .log().all();
    }

    /**
     * Controller method: StatusController.deleteStatus
     * HTTP Method: DELETE
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to delete a status that has assigned tickets.
     * Verification: Confirms the status count remains unchanged.
     */
    @Test
    public void deleteStatus_AssignedTickets_ReturnsConflict() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long statusID = ticketList.get(0).getStatus().getId();

        long statusNumber = statusRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", statusID)
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a status if it has a ticket assigned to it"))
                .log().all();

        assertEquals(statusRepository.count(), statusNumber);
    }

    /**
     * Controller method: StatusController.deleteStatus
     * HTTP Method: DELETE
     * Endpoint: /api/statuses/{statusID}
     * Expected Status: 403 FORBIDDEN
     * Scenario: Attempting to delete a status default status.
     */
    @Test
    public void deleteStatus_DefaultStatus_ReturnsForbidden() {
        Status status = initializeStatus("Pending", false, true);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", status.getId())
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(equalTo("Default ticket status cannot be deleted."))
                .log().all();
    }
}

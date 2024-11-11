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

    //GET: /api/statuses
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllStatuses() {
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

    //GET: /api/statuses/use
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllStatusesWithUseNumbers() throws IOException {
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

        assertEquals(statusList.get(0).useNumber(), 2);
    }

    //GET: /api/statuses/<statusID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the status ID is correct.
    @Test
    public void testGetStatusById() {
        Status status = initializeStatuses().get(0);

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

    //GET: /api/statuses/<statusID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the status ID is incorrect.
    @Test
    public void testGetStatusByIdWhenIdIsWrong() {
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

    //PUT: /api/statuses
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdateStatus() throws JsonProcessingException {
        Status status = initializeStatuses().get(0);

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

    //PUT: /api/statuses
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the status ID is incorrect.
    @Test
    public void testUpdateStatusWhenIdIsWrong() throws JsonProcessingException {
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

    //PUT: /api/statuses
    //Expected status: OK (200)
    //Purpose: Verify the returned status if the new status name is the same as the current name.
    @Test
    public void testUpdateStatusWhenNewNameIsSameAsCurrent() throws JsonProcessingException {
        Status status = initializeStatuses().get(0);

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

    //PUT: /api/statuses
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if status with the given name already exists.
    @Test
    public void testUpdateStatusWhenNewNameIsAlreadyUsed() throws JsonProcessingException {
        List<Status> statusList = initializeStatuses();
        Long statusID = statusList.get(0).getId();
        String statusName = statusList.get(1).getName();

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
    }

    //POST: /api/statuses
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddStatus() throws JsonProcessingException {
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

    //POST: /api/statuses
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if status with the given name already exists.
    @Test
    public void testAddStatusWhenNewNameIsAlreadyUsed() throws JsonProcessingException {
        List<Status> statusList = initializeStatuses();
        String statusName = statusList.get(1).getName();

        AddStatusRequest request = new AddStatusRequest(statusName, true, true);
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
    }

    //DELETE: /api/statuses/<statusID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteStatus() {
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

    //DELETE: /api/statuses/<statusID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the status ID is incorrect.
    @Test
    public void testDeleteStatusWhenIdIsWrong() {
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

    //DELETE: /api/statuses/<statusID>
    //Expected status: CONFLICT (409)
    //Purpose: Verify the returned status if status is assigned to the ticket.
    @Test
    public void testDeleteStatusWhenIsAssignedToTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long statusID = ticketList.get(0).getStatus().getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("statusID", statusID)
                .when()
                .delete("/api/statuses/{statusID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a status if it has a ticket assigned to it"))
                .log().all();
    }

    //DELETE: /api/statuses/<statusID>
    //Expected status: FORBIDDEN (401)
    //Purpose: Verify the returned status if status is default.
    @Test
    public void testDeleteDefaultStatus() {
        Status status = initializeStatuses().get(0);

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

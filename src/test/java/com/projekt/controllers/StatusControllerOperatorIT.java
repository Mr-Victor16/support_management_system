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

    //GET: /api/statuses
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllStatuses() {
        List<Status> statusList = initializeStatus();

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
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testGetStatusById() {
        Status status = initializeStatus().get(0);

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

    //PUT: /api/statuses
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testUpdateStatus() throws JsonProcessingException {
        Status status = initializeStatus().get(0);

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

    //POST: /api/statuses
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testAddStatus() throws JsonProcessingException {
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

    //DELETE: /api/statuses/<statusID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testDeleteStatus() {
        List<Status> statusList = initializeStatus();
        Status status = statusList.get(2);

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

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

    //GET: /api/priorities
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllPriorities() {
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

    //GET: /api/priorities/use
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testGetAllPrioritiesWithUseNumbers() throws IOException {
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

    //GET: /api/priorities/<priorityID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testGetPriorityById() {
        Priority priority = initializePriorities().get(0);

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

    //PUT: /api/priorities
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testUpdatePriority() throws JsonProcessingException {
        Priority priority = initializePriorities().get(0);

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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //POST: /api/priorities
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testAddPriority() throws JsonProcessingException {
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityList.size());
    }

    //DELETE: /api/priorities/<priorityID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testDeletePriority() {
        Priority priority = initializePriorities().get(0);

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

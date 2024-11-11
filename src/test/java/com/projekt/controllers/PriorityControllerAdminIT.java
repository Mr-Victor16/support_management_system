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
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllPrioritiesWithUseNumbers() throws IOException {
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

        assertEquals(responseList.get(1).useNumber(), 2);
    }

    //GET: /api/priorities/<priorityID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the priority ID is correct.
    @Test
    public void testGetPriorityById() {
        Priority priority = initializePriorities().get(0);

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

    //GET: /api/priorities/<priorityID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testGetPriorityByIdWhenIdIsWrong() {
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

    //PUT: /api/priorities
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority updated"))
                .log().all();
    }

    //PUT: /api/priorities
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdatePriorityWhenNewNameIsAlreadyUsed() throws JsonProcessingException {
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

    //PUT: /api/priorities
    //Expected status: OK (200)
    //Purpose: Verify the returned status if the new priority name is the same as the current name.
    @Test
    public void testUpdatePriorityWhenNewNameIsSameAsCurrent() throws JsonProcessingException {
        Priority priority = initializePriorities().get(0);

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

    //PUT: /api/priorities
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testUpdatePriorityWhenIdIsWrong() throws JsonProcessingException {
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

    //POST: /api/priorities
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority added"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityList.size()+1);
    }

    //POST: /api/priorities
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if priority with the given name already exists.
    @Test
    public void testAddPriorityWhenNameAlreadyExists() throws JsonProcessingException {
        Priority priority = initializePriorities().get(0);

        AddPriorityRequest request = new AddPriorityRequest(priority.getName(), priority.getMaxTime());
        ObjectMapper objectMapper = new ObjectMapper();
        String newPriorityJson = objectMapper.writeValueAsString(request);

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
    }

    //DELETE: /api/priorities/<priorityID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeletePriority() {
        List<Priority> priorityList = initializePriorities();
        Priority priority = priorityList.get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priority.getId())
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Priority removed"))
                .log().all();

        assertEquals(priorityRepository.count(), priorityList.size()-1);
    }

    //DELETE: /api/priorities/<priorityID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the priority ID is incorrect.
    @Test
    public void testDeletePriorityWhenIdIsWrong() {
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

    //DELETE: /api/priorities/<priorityID>
    //Expected status: CONFLICT (409)
    //Purpose: Verify the returned status if priority is assigned to the ticket.
    @Test
    public void testDeletePriorityWhenIsAssignedToTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long priorityID = ticketList.get(0).getPriority().getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("priorityID", priorityID)
                .when()
                .delete("/api/priorities/{priorityID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a priority if it has a ticket assigned to it"))
                .log().all();
    }
}

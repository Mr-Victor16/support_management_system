package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;
import com.projekt.repositories.SoftwareRepository;
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

public class SoftwareControllerOperatorIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private SoftwareRepository softwareRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("operator", "operator");
        clearDatabase();
    }

    //GET: /api/software
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllSoftware() {
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

    //GET: /api/software/use
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllSoftwareWithUseNumbers() throws IOException {
        List<Software> softwareList = initializeSoftware();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareList.get(0).getId());
        List<Ticket> ticketList = initializeTicket(softwareList.get(0).getId());

        Response response = RestAssured.given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/software/use")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo((int) softwareRepository.count()))
                .log().all()
                .extract().response();

        List<SoftwareResponse> responseList = response.jsonPath().getList(".", SoftwareResponse.class);

        assertEquals(responseList.get(0).useNumberTicket(), ticketList.size());
        assertEquals(responseList.get(0).useNumberKnowledge(), knowledgeList.size());
        assertEquals(responseList.get(1).useNumberTicket(), 0);
        assertEquals(responseList.get(1).useNumberKnowledge(), 0);
    }

    //GET: /api/software/<softwareID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the software ID is correct.
    @Test
    public void testGetSoftwareById() {
        Software software = initializeSoftware().get(0);

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

    //GET: /api/software/<softwareID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testGetSoftwareByIdWhenIdIsWrong() {
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

    //PUT: /api/software
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testUpdateSoftware() throws JsonProcessingException {
        Software software = initializeSoftware().get(0);

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

    //POST: /api/software
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testAddSoftware() throws JsonProcessingException {
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

    //DELETE: /api/software/<softwareID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testDeleteSoftware() {
        Software software = initializeSoftware().get(0);

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

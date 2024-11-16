package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class KnowledgeBaseControllerOperatorIT extends BaseIntegrationTest {
    private String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("operator", "operator");
        clearDatabase();
    }

    /**
     * Controller method: KnowledgeBaseController.getAllKnowledgeItems
     * HTTP Method: GET
     * Endpoint: /api/knowledge-bases
     * Expected Status: 200 OK
     * Scenario: Retrieve all knowledge items.
     * Verification: Confirms the size of the returned list matches the expected number of elements in the repository.
     */
    @Test
    public void getAllKnowledgeItems_ReturnsKnowledgeListSuccessfully() {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareID);

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(knowledgeList.size()))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.getKnowledgeById
     * HTTP Method: GET
     * Endpoint: /api/knowledge-bases/{knowledgeID}
     * Expected Status: 200 OK
     * Scenario: Retrieve a knowledge item by a valid ID.
     * Verification: Confirms the returned item's details match the expected data.
     */
    @Test
    public void getKnowledgeById_ValidId_ReturnsKnowledgeSuccessfully() {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        Knowledge knowledge = initializeSingleKnowledge("Knowledge name", "First knowledge content", softwareID);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("knowledgeID", knowledge.getId())
                .when()
                .get("/api/knowledge-bases/{knowledgeID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(knowledge.getId().intValue()))
                .body("title", equalTo(knowledge.getTitle()))
                .body("content", equalTo(knowledge.getContent()))
                .body("createdDate", equalTo(knowledge.getCreatedDate().toString()))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.getKnowledgeById
     * HTTP Method: GET
     * Endpoint: /api/knowledge-bases/{knowledgeID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempt to retrieve a knowledge item with an invalid ID.
     */
    @Test
    public void getKnowledgeById_InvalidId_ReturnsNotFound() {
        long knowledgeID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("knowledgeID", knowledgeID)
                .when()
                .get("/api/knowledge-bases/{knowledgeID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Knowledge with ID " + knowledgeID + " not found."))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.updateKnowledge
     * HTTP Method: PUT
     * Endpoint: /api/knowledge-bases
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update knowledge as a user without sufficient permissions.
     */
    @Test
    public void updateKnowledge_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();
        Long softwareID = softwareList.get(0).getId();
        Knowledge knowledge = initializeKnowledge(softwareID).get(0);

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(knowledge.getId(), "new title", "new example knowledge content", softwareList.get(1).getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String updateKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateKnowledgeJson)
                .when()
                .put("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.addKnowledge
     * HTTP Method: POST
     * Endpoint: /api/knowledge-bases
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add knowledge as a user without sufficient permissions.
     */
    @Test
    public void addKnowledge_InsufficientPermissions_ReturnsUnauthorized() throws JsonProcessingException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();

        AddKnowledgeRequest request = new AddKnowledgeRequest("knowledge title", "example knowledge content", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String newKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newKnowledgeJson)
                .when()
                .post("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.deleteKnowledge
     * HTTP Method: DELETE
     * Endpoint: /api/knowledge-bases/{knowledgeID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to delete knowledge as a user without sufficient permissions.
     */
    @Test
    public void deleteKnowledge_InsufficientPermissions_ReturnsUnauthorized() {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        Knowledge knowledge = initializeSingleKnowledge("Knowledge name", "First knowledge content", softwareID);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("knowledgeID", knowledge.getId())
                .when()
                .delete("/api/knowledge-bases/{knowledgeID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import com.projekt.repositories.KnowledgeRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KnowledgeBaseControllerAdminIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
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
        Knowledge knowledge = initializeKnowledge(softwareID).get(0);

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
     * Expected Status: 200 OK
     * Scenario: Update an existing knowledge item with valid data.
     */
    @Test
    public void updateKnowledge_ValidData_ReturnsSuccess() throws JsonProcessingException {
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Knowledge updated"))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.updateKnowledge
     * HTTP Method: PUT
     * Endpoint: /api/knowledge-bases
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempt to update a knowledge item with an invalid software ID.
     */
    @Test
    public void updateKnowledge_InvalidSoftwareId_ReturnsNotFound() throws JsonProcessingException {
        Software software = initializeSingleSoftware("Software name", "Software description");
        long softwareID = 1000;
        Knowledge knowledge = initializeKnowledge(software.getId()).get(0);

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(knowledge.getId(), "new title", "new example knowledge content", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateKnowledgeJson)
                .when()
                .put("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + softwareID + " not found."))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.updateKnowledge
     * HTTP Method: PUT
     * Endpoint: /api/knowledge-bases
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to update knowledge by an invalid ID.
     */
    @Test
    public void updateKnowledge_InvalidId_ReturnsNotFound() throws JsonProcessingException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        long knowledgeID = 1000;

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(knowledgeID, "new title", "new example knowledge content", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateKnowledgeJson)
                .when()
                .put("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Knowledge with ID " + knowledgeID + " not found."))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.updateKnowledge
     * HTTP Method: PUT
     * Endpoint: /api/knowledge-bases
     * Expected Status: 200 OK
     * Scenario: Attempting to update knowledge when the title already exists.
     */
    @Test
    public void updateKnowledge_TitleAlreadyExists_UpdatesSuccessfully() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();
        Long softwareID = softwareList.get(1).getId();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareID);
        Knowledge baseKnowledge = knowledgeList.get(0);
        Knowledge otherKnowledge = knowledgeList.get(1);

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(baseKnowledge.getId(), otherKnowledge.getTitle(), "new example knowledge content", softwareList.get(0).getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String updateKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateKnowledgeJson)
                .when()
                .put("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Knowledge updated"))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.updateKnowledge
     * HTTP Method: PUT
     * Endpoint: /api/knowledge-bases
     * Expected Status: 409 CONFLICT
     * Scenario: Attempt to update a knowledge item with a title that already exists for the same software.
     */
    @Test
    public void updateKnowledge_DuplicateTitleAndSoftware_ReturnsConflict() throws JsonProcessingException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareID);
        Knowledge baseKnowledge = knowledgeList.get(0);
        Knowledge otherKnowledge = knowledgeList.get(1);

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(baseKnowledge.getId(), otherKnowledge.getTitle(), "new example knowledge content", otherKnowledge.getSoftware().getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String updateKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateKnowledgeJson)
                .when()
                .put("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Knowledge with title '" + request.title() + "' and software ID '" + softwareID + "' already exists."))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.addKnowledge
     * HTTP Method: POST
     * Endpoint: /api/knowledge-bases
     * Expected Status: 200 OK
     * Scenario: Add a new knowledge item with valid data.
     * Verification: Confirms the knowledge count increases.
     */
    @Test
    public void addKnowledge_ValidData_ReturnsSuccess() throws JsonProcessingException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        long knowledgeCount = knowledgeRepository.count();

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Knowledge added"))
                .log().all();

        assertEquals(knowledgeRepository.count(), knowledgeCount+1);
    }

    /**
     * Controller method: KnowledgeBaseController.addKnowledge
     * HTTP Method: POST
     * Endpoint: /api/knowledge-bases
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to add knowledge with an invalid software ID.
     */
    @Test
    public void addKnowledge_InvalidSoftwareId_ReturnsNotFound() throws JsonProcessingException {
        long softwareID = 1000;

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
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + request.softwareID() + " not found."))
                .log().all();
    }

    /**
     * Controller method: KnowledgeBaseController.addKnowledge
     * HTTP Method: POST
     * Endpoint: /api/knowledge-bases
     * Expected Status: 200 OK
     * Scenario: Add a knowledge item when the title already exists but under a different software.
     * Verification: Confirms the knowledge count increases.
     */
    @Test
    public void addKnowledge_TitleAlreadyExistsWithDifferentSoftware_ReturnsSuccess() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();
        Long softwareID = softwareList.get(1).getId();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareID);
        Knowledge knowledge = knowledgeList.get(0);

        AddKnowledgeRequest request = new AddKnowledgeRequest(knowledge.getTitle(), "new example knowledge content", softwareList.get(0).getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String addKnowledgeJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(addKnowledgeJson)
                .when()
                .post("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Knowledge added"))
                .log().all();

        assertEquals(knowledgeRepository.count(), knowledgeList.size()+1);
    }

    /**
     * Controller method: KnowledgeBaseController.addKnowledge
     * HTTP Method: POST
     * Endpoint: /api/knowledge-bases
     * Expected Status: 409 CONFLICT
     * Scenario: Attempt to add a knowledge item when the title already exists for the same software.
     * Verification: Confirms the knowledge count remains unchanged.
     */
    @Test
    public void addKnowledge_DuplicateTitleAndSoftware_ReturnsConflict() throws JsonProcessingException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        Knowledge knowledge = initializeKnowledge(softwareID).get(0);

        AddKnowledgeRequest request = new AddKnowledgeRequest(knowledge.getTitle(), "example knowledge content", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String newKnowledgeJson = objectMapper.writeValueAsString(request);

        long knowledgeNumber = knowledgeRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newKnowledgeJson)
                .when()
                .post("/api/knowledge-bases")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Knowledge with title '" + request.title() + "' and software ID '" + softwareID + "' already exists."))
                .log().all();

        assertEquals(knowledgeRepository.count(), knowledgeNumber);
    }

    /**
     * Controller method: KnowledgeBaseController.deleteKnowledge
     * HTTP Method: DELETE
     * Endpoint: /api/knowledge-bases/{knowledgeID}
     * Expected Status: 200 OK
     * Scenario: Delete a knowledge item with a valid ID.
     * Verification: Confirms the knowledge count decreases.
     */
    @Test
    public void deleteKnowledge_ValidId_ReturnsSuccess() {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareID);
        Knowledge knowledge = knowledgeList.get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("knowledgeID", knowledge.getId())
                .when()
                .delete("/api/knowledge-bases/{knowledgeID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Knowledge removed"))
                .log().all();

        assertEquals(knowledgeRepository.count(), knowledgeList.size()-1);
    }

    /**
     * Controller method: KnowledgeBaseController.deleteKnowledge
     * HTTP Method: DELETE
     * Endpoint: /api/knowledge-bases/{knowledgeID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempt to delete a knowledge item with an invalid ID.
     */
    @Test
    public void deleteKnowledge_InvalidId_ReturnsNotFound() {
        long knowledgeID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("knowledgeID", knowledgeID)
                .when()
                .delete("/api/knowledge-bases/{knowledgeID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Knowledge with ID " + knowledgeID + " not found."))
                .log().all();
    }
}

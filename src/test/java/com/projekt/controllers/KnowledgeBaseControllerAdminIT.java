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

    //GET: /api/knowledge-bases
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllKnowledgeItems() {
        List<Software> softwareList = initializeSoftware();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareList.get(0).getId());

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

    //GET: /api/knowledge-bases/<knowledgeID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the knowledge ID is correct.
    @Test
    public void testGetKnowledgeById() {
        List<Software> softwareList = initializeSoftware();
        Knowledge knowledge = initializeKnowledge(softwareList.get(0).getId()).get(0);

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

    //GET: /api/knowledge-bases/<knowledgeID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the knowledge ID is incorrect.
    @Test
    public void testGetKnowledgeByIdWhenIdIsWrong() {
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

    //PUT: /api/knowledge-bases
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdateKnowledge() throws JsonProcessingException {
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

    //PUT: /api/knowledge-bases
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testUpdateKnowledgeWhenSoftwareIdIsWrong() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();
        long softwareID = 1000;
        Knowledge knowledge = initializeKnowledge(softwareList.get(0).getId()).get(0);

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

    //PUT: /api/knowledge-bases
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the knowledge ID is incorrect.
    @Test
    public void testUpdateKnowledgeWhenIdIsWrong() throws JsonProcessingException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //PUT: /api/knowledge-bases
    //Expected status: OK (200)
    //Purpose: Verify the status returned if knowledge with the given title already exists.
    @Test
    public void testUpdateKnowledgeWhenTitleAlreadyExists() throws JsonProcessingException {
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

    //PUT: /api/knowledge-bases
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if knowledge with the given name and software already exists.
    @Test
    public void testUpdateKnowledgeWhenKnowledgeWithGivenTitleAndSoftwareAlreadyExists() throws JsonProcessingException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/knowledge-bases
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testAddKnowledge() throws JsonProcessingException {
        Long softwareID = initializeSoftware().get(0).getId();
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

    //POST: /api/knowledge-bases
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the software ID is incorrect.
    @Test
    public void testAddKnowledgeWhenSoftwareIdIsWrong() throws JsonProcessingException {
        Software software = initializeSoftware().get(0);
        Knowledge knowledge = initializeKnowledge(software.getId()).get(0);
        long softwareID = 1000;

        AddKnowledgeRequest request = new AddKnowledgeRequest(knowledge.getTitle(), "example knowledge content", softwareID);
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

    //POST: /api/knowledge-bases
    //Expected status: OK (200)
    //Purpose: Verify the status returned if knowledge with the given title already exists.
    @Test
    public void testAddKnowledgeWhenTitleAlreadyExists() throws JsonProcessingException {
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

    //POST: /api/knowledge-bases
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if knowledge with the given name already exists.
    @Test
    public void testAddKnowledgeWhenNameAlreadyExists() throws JsonProcessingException {
        Long softwareID = initializeSoftware().get(0).getId();
        Knowledge knowledge = initializeKnowledge(softwareID).get(0);

        AddKnowledgeRequest request = new AddKnowledgeRequest(knowledge.getTitle(), "example knowledge content", softwareID);
        ObjectMapper objectMapper = new ObjectMapper();
        String newKnowledgeJson = objectMapper.writeValueAsString(request);

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
    }

    //DELETE: /api/knowledge-bases/<knowledgeID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteKnowledge() {
        List<Software> softwareList = initializeSoftware();
        List<Knowledge> knowledgeList = initializeKnowledge(softwareList.get(0).getId());
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

    //DELETE: /api/knowledge-bases/<knowledgeID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the knowledge ID is incorrect.
    @Test
    public void testDeleteKnowledgeWhenIdIsWrong() {
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

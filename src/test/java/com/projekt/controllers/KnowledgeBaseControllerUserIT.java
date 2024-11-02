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

public class KnowledgeBaseControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
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
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //POST: /api/knowledge-bases
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
    @Test
    public void testAddKnowledge() throws JsonProcessingException {
        Long softwareID = initializeSoftware().get(0).getId();

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

    //DELETE: /api/knowledge-bases/<knowledgeID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. User doesn't have access rights to this method.
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}



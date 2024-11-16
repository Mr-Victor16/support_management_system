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

public class SoftwareControllerAdminIT extends BaseIntegrationTest {
    private static String jwtToken;

    @Autowired
    private SoftwareRepository softwareRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
        clearDatabase();
    }

    /**
     * Controller method: SoftwareController.getAllSoftware
     * HTTP Method: GET
     * Endpoint: /api/software
     * Expected Status: 200 OK
     * Scenario: Retrieving all software with admin role.
     * Verification: Confirms the returned list size matches the expected software count in the repository.
     */
    @Test
    public void getAllSoftware_ReturnsSoftwareListSuccessfully() {
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

    /**
     * Controller method: SoftwareController.getAllSoftwareWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/software/use
     * Expected Status: 200 OK
     * Scenario: Retrieving all software with use numbers.
     * Verification: Confirms use numbers for tickets and knowledge base are correctly returned.
     */
    @Test
    public void getAllSoftwareWithUseNumbers_ReturnsSoftwareUsageCountListSuccessfully() throws IOException {
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
        assertEquals(0, responseList.get(1).useNumberTicket());
        assertEquals(0, responseList.get(1).useNumberKnowledge());
    }

    /**
     * Controller method: SoftwareController.getSoftwareById
     * HTTP Method: GET
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 200 OK
     * Scenario: Retrieving a software by ID with admin role.
     * Verification: Confirms that the software details returned match the expected values.
     */
    @Test
    public void getSoftwareById_ReturnsSoftwareSuccessfully() {
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

    /**
     * Controller method: SoftwareController.getSoftwareById
     * HTTP Method: GET
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Retrieving a software by incorrect ID with admin role.
     */
    @Test
    public void getSoftwareById_InvalidId_ReturnsNotFound() {
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

    /**
     * Controller method: SoftwareController.updateSoftware
     * HTTP Method: PUT
     * Endpoint: /api/software
     * Expected Status: 200 OK
     * Scenario: Updating software details with valid data.
     */
    @Test
    public void updateSoftware_ValidData_ReturnsSuccess() throws JsonProcessingException {
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Software details updated"))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.updateSoftware
     * HTTP Method: PUT
     * Endpoint: /api/software
     * Expected Status: 409 CONFLICT
     * Scenario: Updating software with an already used name.
     */
    @Test
    public void updateSoftware_NameAlreadyExists_ReturnsConflict() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();
        Software software1 = softwareList.get(0);
        Software software2 = softwareList.get(1);

        UpdateSoftwareRequest request = new UpdateSoftwareRequest(software1.getId(), software2.getName(), "Updated description");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateSoftwareJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateSoftwareJson)
                .when()
                .put("/api/software")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Software with name '" + request.name() + "' already exists."))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.updateSoftware
     * HTTP Method: PUT
     * Endpoint: /api/software
     * Expected Status: 200 OK
     * Scenario: Updating software with the same name.
     */
    @Test
    public void updateSoftware_NewNameIsSameAsCurrent_ReturnsSuccess() throws JsonProcessingException {
        Software software = initializeSoftware().get(0);

        UpdateSoftwareRequest request = new UpdateSoftwareRequest(software.getId(), software.getName(), "Updated description");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateSoftwareJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateSoftwareJson)
                .when()
                .put("/api/software")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Software name is the same as the current name '" + request.name() + "'"))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.addSoftware
     * HTTP Method: POST
     * Endpoint: /api/software
     * Expected Status: 200 OK
     * Scenario: Adding a new software with valid data.
     * Verification: Confirms the software count increases.
     */
    @Test
    public void addSoftware_ValidData_ReturnsSuccess() throws JsonProcessingException {
        List<Software> softwareList = initializeSoftware();

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Software added"))
                .log().all();

        assertEquals(softwareRepository.count(), softwareList.size()+1);
    }

    /**
     * Controller method: SoftwareController.addSoftware
     * HTTP Method: POST
     * Endpoint: /api/software
     * Expected Status: 409 CONFLICT
     * Scenario: Trying to add software with an already existing name.
     * Verification: Confirms the software count remains unchanged.
     */
    @Test
    public void addSoftware_NameAlreadyExists_ReturnsConflict() throws JsonProcessingException {
        Software software = initializeSoftware().get(0);

        AddSoftwareRequest request = new AddSoftwareRequest(software.getName(), "New software description");
        ObjectMapper objectMapper = new ObjectMapper();
        String newSoftwareJson = objectMapper.writeValueAsString(request);

        long softwareNumber = softwareRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newSoftwareJson)
                .when()
                .post("/api/software")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Software with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(softwareRepository.count(), softwareNumber);
    }

    /**
     * Controller method: SoftwareController.deleteSoftware
     * HTTP Method: DELETE
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 200 OK
     * Scenario: Deleting a software with a valid ID.
     * Verification: Confirms the software count decreases.
     */
    @Test
    public void deleteSoftware_NoAssignedTicketAndKnowledge_ReturnsSuccess() {
        List<Software> softwareList = initializeSoftware();
        Software software = softwareList.get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", software.getId())
                .when()
                .delete("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Software removed"))
                .log().all();

        assertEquals(softwareRepository.count(), softwareList.size()-1);
    }

    /**
     * Controller method: SoftwareController.deleteSoftware
     * HTTP Method: DELETE
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Trying to delete a software with an incorrect ID.
     */
    @Test
    public void deleteSoftware_InvalidId_ReturnsNotFound() {
        long softwareID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", softwareID)
                .when()
                .delete("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Software with ID " + softwareID + " not found."))
                .log().all();
    }

    /**
     * Controller method: SoftwareController.deleteSoftware
     * HTTP Method: DELETE
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 409 CONFLICT
     * Scenario: Trying to delete software that is assigned to a ticket.
     * Verification: Confirms the status count remains unchanged.
     */
    @Test
    public void deleteSoftware_AssignedToTicket_ReturnsConflict() throws IOException {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeTicket(softwareID);

        long softwareNumber = softwareRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", softwareID)
                .when()
                .delete("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a software if it has a ticket or knowledge assigned to it"))
                .log().all();

        assertEquals(softwareRepository.count(), softwareNumber);
    }

    /**
     * Controller method: SoftwareController.deleteSoftware
     * HTTP Method: DELETE
     * Endpoint: /api/software/{softwareID}
     * Expected Status: 409 CONFLICT
     * Scenario: Trying to delete software that is assigned to the knowledge base.
     * Verification: Confirms the status count remains unchanged.
     */
    @Test
    public void deleteSoftware_AssignedToKnowledgeBase_ReturnsConflict() {
        Long softwareID = initializeSingleSoftware("Software name", "Software description").getId();
        initializeKnowledge(softwareID);

        long softwareNumber = softwareRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("softwareID", softwareID)
                .when()
                .delete("/api/software/{softwareID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a software if it has a ticket or knowledge assigned to it"))
                .log().all();

        assertEquals(softwareRepository.count(), softwareNumber);
    }
}

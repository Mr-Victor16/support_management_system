package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Role;
import com.projekt.repositories.RoleRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RoleControllerIT extends BaseIntegrationTest {
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Controller method: RoleController.getAllRoles
     * HTTP Method: GET
     * Endpoint: /api/roles
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to access roles list as a user without sufficient permissions (as USER).
     */
    @Test
    public void getAllRoles_WithUserRole_ReturnsUnauthorized() throws JsonProcessingException {
        String jwtToken = getJwtToken("user", "user");

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/roles")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    /**
     * Controller method: RoleController.getAllRoles
     * HTTP Method: GET
     * Endpoint: /api/roles
     * Expected Status: 200 OK
     * Scenario: Accessing the roles list as OPERATOR.
     * Verification: Verifies the returned list size matches the number of roles in the repository.
     */
    @Test
    public void getAllRoles_WithOperatorRole_ReturnsRolesListSuccessfully() throws JsonProcessingException {
        String jwtToken = getJwtToken("operator", "operator");
        List<Role> roleList = roleRepository.findAll();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/roles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(roleList.size()))
                .log().all();
    }

    /**
     * Controller method: RoleController.getAllRoles
     * HTTP Method: GET
     * Endpoint: /api/roles
     * Expected Status: 200 OK
     * Scenario: Accessing the roles list as ADMIN.
     * Verification: Verifies the returned list size matches the number of roles in the repository.
     */
    @Test
    public void getAllRoles_WithAdminRole_ReturnsRolesListSuccessfully() throws JsonProcessingException {
        String jwtToken = getJwtToken("admin", "admin");
        List<Role> roleList = roleRepository.findAll();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/roles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(roleList.size()))
                .log().all();
    }
}

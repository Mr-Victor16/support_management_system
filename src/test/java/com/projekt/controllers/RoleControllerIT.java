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

    //GET: /api/roles
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned. User doesn't have access rights to this method.
    @Test
    public void testGetAllRolesAsUser() throws JsonProcessingException {
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

    //GET: /api/roles
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllRolesAsOperator() throws JsonProcessingException {
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

    //GET: /api/roles
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllRolesAsAdmin() throws JsonProcessingException {
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

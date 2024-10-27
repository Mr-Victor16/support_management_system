package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Category;
import com.projekt.models.Software;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.repositories.CategoryRepository;
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

public class CategoryControllerUserIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("user", "user");
        clearDatabase();
    }

    //GET: /api/categories
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllCategories() {
        List<Category> categoryList = initializeCategory();

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo(categoryList.size()))
                .log().all();
    }

    //GET: /api/categories/use
    //Expected status: OK (200)
    //Purpose: To verify the returned status and the expected number of elements.
    @Test
    public void testGetAllCategoriesWithUseNumbers() throws IOException {
        List<Software> softwareList = initializeSoftware();
        initializeTicket(softwareList.get(0).getId());

        given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/categories/use")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //GET: /api/categories/<categoryID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testGetCategoryById() {
        Category category = initializeCategory().get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", category.getId())
                .when()
                .get("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //PUT: /api/categories
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testUpdateCategory() throws JsonProcessingException {
        Category category = initializeCategory().get(0);

        UpdateCategoryRequest request = new UpdateCategoryRequest(category.getId(), "Updated category");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateCategoryJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateCategoryJson)
                .when()
                .put("/api/categories")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }

    //POST: /api/categories
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testAddCategory() throws JsonProcessingException {
        List<Category> categoryList = initializeCategory();

        AddCategoryRequest request = new AddCategoryRequest("New category");
        ObjectMapper objectMapper = new ObjectMapper();
        String newCategoryJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newCategoryJson)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();

        assertEquals(categoryRepository.count(), categoryList.size());
    }

    //DELETE: /api/categories/<categoryID>
    //Expected status: UNAUTHORIZED (401)
    //Purpose: Verify the status returned if the request contains valid data. Operator doesn't have access rights to this method.
    @Test
    public void testDeleteCategory() {
        Category category = initializeCategory().get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", category.getId())
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Full authentication is required to access this resource"))
                .log().all();
    }
}

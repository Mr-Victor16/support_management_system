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

    /**
     * Controller method: CategoryController.getAllCategories
     * HTTP Method: GET
     * Endpoint: /api/categories
     * Expected Status: 200 OK
     * Scenario: Retrieving all categories with user role.
     * Verification: Confirms the returned list size matches the expected category count in the repository.
     */
    @Test
    public void getAllCategories_ReturnsCategoryListSuccessfully() {
        List<Category> categoryList = initializeCategories();

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

    /**
     * Controller method: CategoryController.getAllCategoriesWithUseNumbers
     * HTTP Method: GET
     * Endpoint: /api/categories/use
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve all categories with associated usage numbers as a user without sufficient permissions (as USER).
     */
    @Test
    public void getAllCategoriesWithUseNumbers_WithUserRole_ReturnsUnauthorized() throws IOException {
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

    /**
     * Controller method: CategoryController.getCategoryById
     * HTTP Method: GET
     * Endpoint: /api/categories/{userID}
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to retrieve category by ID as a user without sufficient permissions (as USER).
     */
    @Test
    public void getCategoryById_WithUserRole_ReturnsUnauthorized() {
        Category category = initializeCategories().get(0);

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

    /**
     * Controller method: CategoryController.updateCategory
     * HTTP Method: PUT
     * Endpoint: /api/categories
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to update category as a user without sufficient permissions (as USER).
     */
    @Test
    public void updateCategory_WithUserRole_ReturnsUnauthorized() throws JsonProcessingException {
        Category category = initializeCategories().get(0);

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

    /**
     * Controller method: CategoryController.addCategory
     * HTTP Method: POST
     * Endpoint: /api/categories
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to add category as a user without sufficient permissions (as USER).
     */
    @Test
    public void addCategory_WithUserRole_ReturnsUnauthorized() throws JsonProcessingException {
        List<Category> categoryList = initializeCategories();

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

    /**
     * Controller method: CategoryController.deleteCategory
     * HTTP Method: DELETE
     * Endpoint: /api/categories
     * Expected Status: 401 UNAUTHORIZED
     * Scenario: Attempting to delete category as a user without sufficient permissions (as USER).
     */
    @Test
    public void deleteCategory_WithUserRole_ReturnsUnauthorized() {
        Category category = initializeCategories().get(0);

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

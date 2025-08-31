package com.projekt.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.BaseIntegrationTest;
import com.projekt.models.Category;
import com.projekt.models.Software;
import com.projekt.models.Ticket;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.repositories.CategoryRepository;
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

public class CategoryControllerAdminIT extends BaseIntegrationTest {
    private String jwtToken;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUpTestData() throws JsonProcessingException {
        jwtToken = getJwtToken("admin", "admin");
        clearDatabase();
    }

    /**
     * Controller method: CategoryController.getAllCategories
     * HTTP Method: GET
     * Endpoint: /api/categories
     * Expected Status: 200 OK
     * Scenario: Retrieving all categories.
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
     * Expected Status: 200 OK
     * Scenario: Retrieving all categories with associated usage numbers.
     * Verification: Confirms the correct usage count for each category.
     */
    @Test
    public void getAllCategoriesWithUseNumbers_ReturnsCategoryUsageCountListSuccessfully() throws IOException {
        List<Software> softwareList = initializeSoftware();
        initializeTicket(softwareList.get(0).getId());

        Response response = RestAssured.given()
                .auth().oauth2(jwtToken)
                .when()
                .get("/api/categories/use")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", equalTo((int) categoryRepository.count()))
                .log().all()
                .extract().response();

        List<CategoryResponse> responseList = response.jsonPath().getList(".", CategoryResponse.class);

        assertEquals(2, responseList.get(0).useNumber());
    }

    /**
     * Controller method: CategoryController.getCategoryById
     * HTTP Method: GET
     * Endpoint: /api/categories/{categoryID}
     * Expected Status: 200 OK
     * Scenario: Retrieving a category by its valid ID.
     * Verification: Confirms the category's details match the expected values.
     */
    @Test
    public void getCategoryById_ReturnsCategorySuccessfully() {
        Category category = initializeCategory("General");

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", category.getId())
                .when()
                .get("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(category.getId().intValue()))
                .body("name", equalTo(category.getName()))
                .log().all();
    }

    /**
     * Controller method: CategoryController.getCategoryById
     * HTTP Method: GET
     * Endpoint: /api/categories/{categoryID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to retrieve a category by an invalid ID.
     */
    @Test
    public void getCategoryById_InvalidId_ReturnsNotFound() {
        long categoryID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", categoryID)
                .when()
                .get("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Category with ID " + categoryID + " not found."))
                .log().all();
    }

    /**
     * Controller method: CategoryController.updateCategory
     * HTTP Method: PUT
     * Endpoint: /api/categories
     * Expected Status: 200 OK
     * Scenario: Updating a category with valid data.
     */
    @Test
    public void updateCategory_ValidData_ReturnsSuccess() throws JsonProcessingException {
        Category category = initializeCategory("General");

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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category updated"))
                .log().all();
    }

    /**
     * Controller method: CategoryController.updateCategory
     * HTTP Method: PUT
     * Endpoint: /api/categories
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to update a category to a name that already exists.
     * Verification: Confirms the category count remains unchanged.
     */
    @Test
    public void updateCategory_ExistingName_ReturnsConflict() throws JsonProcessingException {
        List<Category> categoryList = initializeCategories();
        Long categoryID = categoryList.get(0).getId();
        String categoryName = categoryList.get(1).getName();

        long categoryNumber = categoryRepository.count();

        UpdateCategoryRequest request = new UpdateCategoryRequest(categoryID, categoryName);
        ObjectMapper objectMapper = new ObjectMapper();
        String updateSoftwareJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateSoftwareJson)
                .when()
                .put("/api/categories")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Category with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(categoryRepository.count(), categoryNumber);
    }

    /**
     * Controller method: CategoryController.updateCategory
     * HTTP Method: PUT
     * Endpoint: /api/categories
     * Expected Status: 200 OK
     * Scenario: Attempting to update a category with the same name as the current one.
     */
    @Test
    public void updateCategory_SameNameAsCurrent_ReturnsSuccess() throws JsonProcessingException {
        Category category = initializeCategory("General");

        UpdateCategoryRequest request = new UpdateCategoryRequest(category.getId(), category.getName());
        ObjectMapper objectMapper = new ObjectMapper();
        String updateCategoryJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateCategoryJson)
                .when()
                .put("/api/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category updated"))
                .log().all();
    }

    /**
     * Controller method: CategoryController.updateCategory
     * HTTP Method: PUT
     * Endpoint: /api/categories
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to update a category by an invalid ID.
     */
    @Test
    public void updateCategory_InvalidId_ReturnsNotFound() throws JsonProcessingException {
        long categoryID = 1000;

        UpdateCategoryRequest request = new UpdateCategoryRequest(categoryID, "Updated category");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateCategoryJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(updateCategoryJson)
                .when()
                .put("/api/categories")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Category with ID " + categoryID + " not found."))
                .log().all();
    }

    /**
     * Controller method: CategoryController.addCategory
     * HTTP Method: POST
     * Endpoint: /api/categories
     * Expected Status: 200 OK
     * Scenario: Adding a new category with a unique name.
     * Verification: Confirms the category count increases.
     */
    @Test
    public void addCategory_UniqueName_ReturnsSuccess() throws JsonProcessingException {
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category added"))
                .log().all();

        assertEquals(categoryRepository.count(), categoryList.size()+1);
    }

    /**
     * Controller method: CategoryController.addCategory
     * HTTP Method: POST
     * Endpoint: /api/categories
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to add a category with a name that already exists.
     * Verification: Confirms the category count remains unchanged.
     */
    @Test
    public void addCategory_ExistingName_ReturnsConflict() throws JsonProcessingException {
        Category category = initializeCategory("General");
        long categoryNumber = categoryRepository.count();

        AddCategoryRequest request = new AddCategoryRequest(category.getName());
        ObjectMapper objectMapper = new ObjectMapper();
        String newCategoryJson = objectMapper.writeValueAsString(request);

        given()
                .auth().oauth2(jwtToken)
                .contentType(ContentType.JSON)
                .body(newCategoryJson)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("Category with name '" + request.name() + "' already exists."))
                .log().all();

        assertEquals(categoryRepository.count(), categoryNumber);
    }

    /**
     * Controller method: CategoryController.deleteCategory
     * HTTP Method: DELETE
     * Endpoint: /api/categories/{categoryID}
     * Expected Status: 200 OK
     * Scenario: Deleting a category with no associated tickets.
     * Verification: Confirms the category count decreases.
     */
    @Test
    public void deleteCategory_NoAssignedTickets_ReturnsSuccess() {
        List<Category> categoryList = initializeCategories();
        Category category = categoryList.get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", category.getId())
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category removed"))
                .log().all();

        assertEquals(categoryRepository.count(), categoryList.size()-1);
    }

    /**
     * Controller method: CategoryController.deleteCategory
     * HTTP Method: DELETE
     * Endpoint: /api/categories/{categoryID}
     * Expected Status: 404 NOT FOUND
     * Scenario: Attempting to delete a category by an invalid ID.
     */
    @Test
    public void deleteCategory_InvalidId_ReturnsNotFound() {
        long categoryID = 1000;

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", categoryID)
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Category with ID " + categoryID + " not found."))
                .log().all();
    }

    /**
     * Controller method: CategoryController.deleteCategory
     * HTTP Method: DELETE
     * Endpoint: /api/categories/{categoryID}
     * Expected Status: 409 CONFLICT
     * Scenario: Attempting to delete a category that has assigned tickets.
     * Verification: Confirms the category count remains unchanged.
     */
    @Test
    public void deleteCategory_AssignedTickets_ReturnsConflict() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long categoryID = ticketList.get(0).getCategory().getId();

        long categoryNumber = categoryRepository.count();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", categoryID)
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a category if it has a ticket assigned to it"))
                .log().all();

        assertEquals(categoryRepository.count(), categoryNumber);
    }
}

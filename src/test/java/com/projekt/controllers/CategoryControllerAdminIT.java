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

        assertEquals(responseList.get(0).useNumber(), 2);
    }

    //GET: /api/categories/<categoryID>
    //Expected status: OK (200)
    //Purpose: Verify the returned status when the category ID is correct.
    @Test
    public void testGetCategoryById() {
        Category category = initializeCategory().get(0);

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

    //GET: /api/categories/<categoryID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testGetCategoryByIdWhenIdIsWrong() {
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

    //PUT: /api/categories
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category updated"))
                .log().all();
    }

    //PUT: /api/categories
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testUpdateCategoryWhenNewNameIsAlreadyUsed() throws JsonProcessingException {
        List<Category> categoryList = initializeCategory();
        Long categoryID = categoryList.get(0).getId();
        String categoryName = categoryList.get(1).getName();

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
    }

    //PUT: /api/categories
    //Expected status: OK (200)
    //Purpose: Verify the returned status if the new category name is the same as the current name.
    @Test
    public void testUpdateCategoryWhenNewNameIsSameAsCurrent() throws JsonProcessingException {
        Category category = initializeCategory().get(0);

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
                .body(equalTo("Category name is the same as the current name '" + request.name() + "'"))
                .log().all();
    }

    //PUT: /api/categories
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testUpdateCategoryWhenIdIsWrong() throws JsonProcessingException {
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

    //POST: /api/categories
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
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
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category added"))
                .log().all();

        assertEquals(categoryRepository.count(), categoryList.size()+1);
    }

    //POST: /api/categories
    //Expected status: CONFLICT (409)
    //Purpose: Verify the status returned if category with the given name already exists.
    @Test
    public void testAddCategoryWhenNameAlreadyExists() throws JsonProcessingException {
        Category category = initializeCategory().get(0);

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
    }

    //DELETE: /api/categories/<categoryID>
    //Expected status: OK (200)
    //Purpose: Verify the status returned if the request contains valid data.
    @Test
    public void testDeleteCategory() {
        Category category = initializeCategory().get(0);

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", category.getId())
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Category removed"))
                .log().all();
    }

    //DELETE: /api/categories/<categoryID>
    //Expected status: NOT FOUND (404)
    //Purpose: Verify the returned status when the category ID is incorrect.
    @Test
    public void testDeleteCategoryWhenIdIsWrong() {
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

    //DELETE: /api/categories/<categoryID>
    //Expected status: CONFLICT (409)
    //Purpose: Verify the returned status if category is assigned to the ticket.
    @Test
    public void testDeleteCategoryWhenIsAssignedToTicket() throws IOException {
        Long softwareID = initializeSoftware().get(0).getId();
        List<Ticket> ticketList = initializeTicket(softwareID);
        Long categoryID = ticketList.get(0).getCategory().getId();

        given()
                .auth().oauth2(jwtToken)
                .pathParam("categoryID", categoryID)
                .when()
                .delete("/api/categories/{categoryID}")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("You cannot remove a category if it has a ticket assigned to it"))
                .log().all();
    }
}

package com.projekt.controllers;

import com.projekt.models.Category;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<List<Category>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesWithUseNumber(){
        return ResponseEntity.ok(categoryService.getAllWithUseNumber());
    }

    @GetMapping("/{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> getCategoryById(@PathVariable("categoryID") Long categoryID) {
        return ResponseEntity.ok(categoryService.loadById(categoryID));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateCategory(@RequestBody @Valid UpdateCategoryRequest request){
        categoryService.update(request);
        return ResponseEntity.ok("Category name updated");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addCategory(@RequestBody @Valid AddCategoryRequest request){
        categoryService.add(request);
        return ResponseEntity.ok("Category added");
    }

    @DeleteMapping("{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable(name = "categoryID") Long categoryID){
        categoryService.delete(categoryID);
        return new ResponseEntity<>("Category removed successfully", HttpStatus.OK);
    }
}

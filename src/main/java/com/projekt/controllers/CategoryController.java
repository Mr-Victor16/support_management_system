package com.projekt.controllers;

import com.projekt.models.Category;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.services.CategoryService;
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
    public List<Category> getAllCategories(){
        return categoryService.getAll();
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public List<CategoryResponse> getAllCategoriesWithUseNumber(){
        return categoryService.getAllWithUseNumber();
    }

    @GetMapping("/{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category getCategoryById(@PathVariable("categoryID") Long categoryID) {
        return categoryService.loadById(categoryID);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategory(@RequestBody @Valid UpdateCategoryRequest request){
        categoryService.update(request);
        return "Category updated";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String addCategory(@RequestBody @Valid AddCategoryRequest request){
        categoryService.add(request);
        return "Category added";
    }

    @DeleteMapping("{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable(name = "categoryID") Long categoryID){
        categoryService.delete(categoryID);
        return "Category removed";
    }
}

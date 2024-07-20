package com.projekt.controllers;

import com.projekt.payload.request.AddCategoryRequest;
import com.projekt.payload.request.EditCategoryRequest;
import com.projekt.services.CategoryService;
import com.projekt.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final TicketService ticketService;

    public CategoryController(CategoryService categoryService, TicketService ticketService) {
        this.categoryService = categoryService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllCategoriesWithUseNumber(){
        return ResponseEntity.ok(categoryService.getAllWithUseNumber());
    }

    @GetMapping("{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCategoryById(@PathVariable(name = "categoryID") Long categoryID){
        if(categoryService.existsById(categoryID)){
            return ResponseEntity.ok(categoryService.loadById(categoryID));
        }

        return new ResponseEntity<>("No category found", HttpStatus.NOT_FOUND);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editCategory(@RequestBody @Valid EditCategoryRequest request){
        if(!categoryService.existsById(request.getCategoryId())){
            return new ResponseEntity<>("No category found", HttpStatus.NOT_FOUND);
        }

        if(categoryService.loadById(request.getCategoryId()).getName().equals(request.getCategoryName())){
            return new ResponseEntity<>("Category name is the same as the current name", HttpStatus.OK);
        }

        if(!categoryService.existsByName(request.getCategoryName())){
            categoryService.update(request);
            return new ResponseEntity<>("Category name edited", HttpStatus.OK);
        }

        return new ResponseEntity<>("Category already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addCategory(@RequestBody @Valid AddCategoryRequest request){
        if(!categoryService.existsByName(request.getCategoryName())){
            categoryService.save(request.getCategoryName());
            return new ResponseEntity<>("Category added", HttpStatus.OK);
        }

        return new ResponseEntity<>("Category already exists", HttpStatus.CONFLICT);
    }

    @DeleteMapping("{categoryID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable(name = "categoryID") Long categoryID){
        if(!categoryService.existsById(categoryID)){
            return new ResponseEntity<>("No category found", HttpStatus.NOT_FOUND);
        }

        if(!ticketService.existsByCategoryId(categoryID)){
            categoryService.delete(categoryID);
            return new ResponseEntity<>("Category removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a category if it has a ticket assigned to it", HttpStatus.CONFLICT);
    }
}

package com.projekt.services;

import com.projekt.models.Category;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    Category loadById(Long id);

    boolean existsById(Long id);

    void add(AddCategoryRequest request);

    void delete(Long id);

    List<Category> getAll();

    void update(UpdateCategoryRequest request);

    List<CategoryResponse> getAllWithUseNumber();
}

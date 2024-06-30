package com.projekt.services;

import com.projekt.models.Category;
import com.projekt.payload.request.EditCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    Category loadById(Long id);

    boolean existsById(Long id);

    void save(String categoryName);

    void delete(Long id);

    List<Category> getAll();

    boolean findByName(String name);

    void update(EditCategoryRequest request);

    List<CategoryResponse> getAllWithUseNumber();
}

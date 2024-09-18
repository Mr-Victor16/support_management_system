package com.projekt.services;

import com.projekt.converter.CategoryConverter;
import com.projekt.exceptions.*;
import com.projekt.models.Category;
import com.projekt.payload.request.add.AddCategoryRequest;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.repositories.CategoryRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("categoryDetailsService")
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final CategoryConverter categoryConverter;

    public CategoryServiceImpl(CategoryRepository categoryRepository, TicketRepository ticketRepository, CategoryConverter categoryConverter) {
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
        this.categoryConverter = categoryConverter;
    }

    @Override
    public Category loadById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public void add(AddCategoryRequest request) {
        if(categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Category", request.name());
        }

        categoryRepository.save(new Category(request.name()));
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));

        if(ticketRepository.existsByCategoryId(category.getId())){
            throw new ResourceHasAssignedItemsException("category", "ticket");
        }

        categoryRepository.deleteById(category.getId());
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public void update(UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(request.categoryID())
                .orElseThrow(() -> new NotFoundException("Category", request.categoryID()));

        if(category.getName().equals(request.name())) {
            throw new NameUnchangedException("Category", request.name());
        }

        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Category", request.name());
        }

        category.setName(request.name());
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryResponse> getAllWithUseNumber() {
        return categoryRepository.findAll().stream()
                .map(category -> categoryConverter.toCategoryResponse(category))
                .toList();
    }
}

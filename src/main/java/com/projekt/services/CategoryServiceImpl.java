package com.projekt.services;

import com.projekt.models.Category;
import com.projekt.payload.request.update.UpdateCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.repositories.CategoryRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("categoryDetailsService")
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, TicketRepository ticketRepository) {
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Category loadById(Long id) {
        return categoryRepository.getReferenceById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public void save(String categoryName) {
        categoryRepository.save(new Category(categoryName));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByNameIgnoreCase(categoryName);
    }

    @Override
    public void update(UpdateCategoryRequest request) {
        Category category = categoryRepository.getReferenceById(request.categoryID());
        category.setName(request.name());
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryResponse> getAllWithUseNumber() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        ticketRepository.countByCategoryId(category.getId())
                ))
                .collect(Collectors.toList());
    }
}

package com.projekt.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.projekt.models.Category;
import com.projekt.payload.request.EditCategoryRequest;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.repositories.CategoryRepository;
import com.projekt.repositories.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CategoryServiceTests {
    private CategoryService categoryService;
    private TicketRepository ticketRepository;
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUp() {
        ticketRepository = mock(TicketRepository.class);
        categoryRepository = mock(CategoryRepository.class);

        categoryService = new CategoryServiceImpl(categoryRepository, ticketRepository);
    }

    //void update(EditCategoryRequest request);
    //Tests the update method to ensure the category is correctly updated based on the data from the EditCategoryRequest object.
    @Test
    void shouldUpdateCategory() {
        Long categoryID = 1L;
        String categoryName = "Updated Category";
        EditCategoryRequest request = new EditCategoryRequest(categoryID, categoryName);
        Category category = new Category(categoryID, "Original category name");

        when(categoryRepository.getReferenceById(categoryID)).thenReturn(category);

        categoryService.update(request);

        assertEquals(categoryName, category.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    //List<CategoryResponse> getAllWithUseNumber();
    //Tests the getAllWithUseNumber method to ensure it returns a list of categories along with their associated usage counts.
    @Test
    void shouldReturnCategoriesWithUsageCounts() {
        Category category1 = new Category(1L, "CategoryName1");
        Category category2 = new Category(2L, "CategoryName2");
        List<Category> categories = List.of(category1, category2);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(ticketRepository.countByCategoryId(category1.getId())).thenReturn(5L);
        when(ticketRepository.countByCategoryId(category2.getId())).thenReturn(10L);

        List<CategoryResponse> result = categoryService.getAllWithUseNumber();

        assertEquals(categories.size(), result.size());
        assertEquals(category1.getId(), result.get(0).getCategoryId());
        assertEquals(5L, result.get(0).getUseNumber());
        assertEquals(category2.getId(), result.get(1).getCategoryId());
        assertEquals(10L, result.get(1).getUseNumber());
    }
}

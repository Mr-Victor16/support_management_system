package com.projekt.converter;

import com.projekt.models.Category;
import com.projekt.payload.response.CategoryResponse;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {
    private final TicketRepository ticketRepository;

    public CategoryConverter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                ticketRepository.countByCategoryId(category.getId())
        );
    }
}

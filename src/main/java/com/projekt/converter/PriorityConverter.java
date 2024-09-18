package com.projekt.converter;

import com.projekt.models.Priority;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Component;

@Component
public class PriorityConverter {
    private final TicketRepository ticketRepository;

    public PriorityConverter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public PriorityResponse toPriorityResponse(Priority priority){
        return new PriorityResponse(
                priority.getId(),
                priority.getName(),
                ticketRepository.countByPriorityId(priority.getId())
        );
    }
}

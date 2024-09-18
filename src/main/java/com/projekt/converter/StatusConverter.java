package com.projekt.converter;

import com.projekt.models.Status;
import com.projekt.payload.response.StatusResponse;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Component;

@Component
public class StatusConverter {
    private final TicketRepository ticketRepository;

    public StatusConverter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public StatusResponse toStatusResponse(Status status){
        return new StatusResponse(
                status.getId(),
                status.getName(),
                status.isCloseTicket(),
                status.isDefaultStatus(),
                ticketRepository.countByStatusId(status.getId())
        );
    }
}

package com.projekt.converter;

import com.projekt.models.Software;
import com.projekt.payload.response.SoftwareResponse;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Component;

@Component
public class SoftwareConverter {
    private final KnowledgeRepository knowledgeRepository;
    private final TicketRepository ticketRepository;

    public SoftwareConverter(KnowledgeRepository knowledgeRepository, TicketRepository ticketRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.ticketRepository = ticketRepository;
    }

    public SoftwareResponse toSoftwareResponse(Software software){
        return new SoftwareResponse(
                software.getId(),
                software.getName(),
                software.getDescription(),
                ticketRepository.countBySoftwareId(software.getId()),
                knowledgeRepository.countBySoftwareId(software.getId())
        );
    }
}

package com.projekt.services;

import com.projekt.exceptions.*;
import com.projekt.models.Software;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("softwareDetailsService")
public class SoftwareServiceImpl implements SoftwareService {
    private final SoftwareRepository softwareRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final TicketRepository ticketRepository;

    public SoftwareServiceImpl(SoftwareRepository softwareRepository, KnowledgeRepository knowledgeRepository, TicketRepository ticketRepository) {
        this.softwareRepository = softwareRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<Software> getAll() {
        return softwareRepository.findAll();
    }

    @Override
    public Software loadById(Long id) {
        return softwareRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Software", id));
    }

    @Override
    public boolean existsById(Long id) {
        return softwareRepository.existsById(id);
    }

    @Override
    public void delete(Long id) {
        Software software = softwareRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Software", id));

        if(ticketRepository.existsBySoftwareId(software.getId()) || knowledgeRepository.existsBySoftwareId(software.getId())) {
            throw new ResourceHasAssignedItemsException("software", "ticket or knowledge");
        }

        softwareRepository.deleteById(software.getId());
    }

    @Override
    public void update(UpdateSoftwareRequest request) {
        Software software = softwareRepository.findById(request.softwareID())
                .orElseThrow(() -> new NotFoundException("Software", request.softwareID()));

        if(software.getName().equals(request.name())) {
            throw new NameUnchangedException("Software", request.name());
        }

        if(softwareRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Software", request.name());
        }

        software.setName(request.name());
        software.setDescription(request.description());
        softwareRepository.save(software);
    }

    @Override
    public void add(AddSoftwareRequest request) {
        if(softwareRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Software", request.name());
        }

        softwareRepository.save(new Software(request.name(), request.description()));
    }

    @Override
    public List<SoftwareResponse> getAllWithUseNumber() {
        return softwareRepository.findAll().stream()
                .map(software -> new SoftwareResponse(
                        software.getId(),
                        software.getName(),
                        software.getDescription(),
                        ticketRepository.countBySoftwareId(software.getId()),
                        knowledgeRepository.countBySoftwareId(software.getId())
                ))
                .toList();
    }
}

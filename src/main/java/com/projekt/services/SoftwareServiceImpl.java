package com.projekt.services;

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
        return softwareRepository.getReferenceById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return softwareRepository.existsById(id);
    }

    @Override
    public void delete(Long id) {
        softwareRepository.deleteById(id);
    }

    @Override
    public void update(UpdateSoftwareRequest request) {
        Software software = softwareRepository.getReferenceById(request.softwareID());
        software.setName(request.name());
        software.setDescription(request.description());
        softwareRepository.save(software);
    }

    @Override
    public boolean existsByName(String softwareName) {
        return softwareRepository.existsByNameIgnoreCase(softwareName);
    }

    @Override
    public void save(AddSoftwareRequest request) {
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

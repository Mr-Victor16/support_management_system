package com.projekt.services;

import com.projekt.converter.PriorityConverter;
import com.projekt.exceptions.*;
import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.repositories.PriorityRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service("priorityDetailsService")
public class PriorityServiceImpl implements PriorityService{
    private final PriorityRepository priorityRepository;
    private final TicketRepository ticketRepository;
    private final PriorityConverter priorityConverter;

    public PriorityServiceImpl(PriorityRepository priorityRepository, TicketRepository ticketRepository, PriorityConverter priorityConverter) {
        this.priorityRepository = priorityRepository;
        this.ticketRepository = ticketRepository;
        this.priorityConverter = priorityConverter;
    }

    @Override
    public Priority loadById(Long id) {
        return priorityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Priority", id));
    }

    @Override
    public boolean existsById(Long id) {
        return priorityRepository.existsById(id);
    }

    @Override
    public void update(UpdatePriorityRequest request) {
        Priority priority = priorityRepository.findById(request.priorityID())
                .orElseThrow(() -> new NotFoundException("Priority", request.priorityID()));

        if(!Objects.equals(priority.getName(), request.name()) && priorityRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Priority", request.name());
        }

        priority.setName(request.name());
        priorityRepository.save(priority);
    }

    @Override
    public void add(AddPriorityRequest request) {
        if(priorityRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Priority", request.name());
        }

        priorityRepository.save(new Priority(request.name()));
    }

    @Override
    public void delete(Long id) {
        Priority priority = priorityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Priority", id));

        if(ticketRepository.existsByPriorityId(priority.getId())){
            throw new ResourceHasAssignedItemsException("priority", "ticket");
        }

        priorityRepository.deleteById(id);
    }

    @Override
    public List<Priority> getAll() {
        return priorityRepository.findAll();
    }

    @Override
    public List<PriorityResponse> getAllWithUseNumber(){
        return priorityRepository.findAll().stream()
                .map(priority -> priorityConverter.toPriorityResponse(priority))
                .toList();
    }
}

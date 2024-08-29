package com.projekt.services;

import com.projekt.exceptions.*;
import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.repositories.PriorityRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("priorityDetailsService")
public class PriorityServiceImpl implements PriorityService{
    private final PriorityRepository priorityRepository;
    private final TicketRepository ticketRepository;

    public PriorityServiceImpl(PriorityRepository priorityRepository, TicketRepository ticketRepository) {
        this.priorityRepository = priorityRepository;
        this.ticketRepository = ticketRepository;
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

        if(priority.getName().equals(request.name())) {
            throw new NameUnchangedException("Priority", request.name());
        }

        if(priorityRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Priority", request.name());
        }

        priority.setName(request.name());
        priority.setMaxTime(request.maxTime());
        priorityRepository.save(priority);
    }

    @Override
    public void add(AddPriorityRequest request) {
        if(priorityRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Priority", request.name());
        }

        priorityRepository.save(new Priority(request.name(), request.maxTime()));
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
                .map(priority -> new PriorityResponse(
                        priority.getId(),
                        priority.getName(),
                        ticketRepository.countByPriorityId(priority.getId())
                ))
                .toList();
    }
}

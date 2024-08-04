package com.projekt.services;

import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.edit.EditPriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.repositories.PriorityRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        return priorityRepository.getReferenceById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return priorityRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String priorityName) {
        return priorityRepository.existsByNameIgnoreCase(priorityName);
    }

    @Override
    public void update(EditPriorityRequest request) {
        Priority priority = priorityRepository.getReferenceById(request.getPriorityId());
        priority.setName(request.getPriorityName());
        priority.setMaxTime(request.getMaxTime());
        priorityRepository.save(priority);
    }

    @Override
    public void save(AddPriorityRequest request) {
        priorityRepository.save(new Priority(request.getPriorityName(), request.getMaxTime()));
    }

    @Override
    public void delete(Long id) {
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
                .collect(Collectors.toList());
    }
}

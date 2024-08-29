package com.projekt.services;

import com.projekt.exceptions.*;
import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;
import com.projekt.repositories.StatusRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("statusDetailsService")
public class StatusServiceImpl implements StatusService{
    private final StatusRepository statusRepository;
    private final TicketRepository ticketRepository;

    public StatusServiceImpl(StatusRepository statusRepository, TicketRepository ticketRepository) {
        this.statusRepository = statusRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Status loadById(Long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Status", id));
    }

    @Override
    public boolean existsById(Long id) {
        return statusRepository.existsById(id);
    }

    @Override
    public void add(AddStatusRequest request) {
        if(statusRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Status", request.name());
        }

        statusRepository.save(new Status(request.name(), request.closeTicket()));
    }

    @Override
    public List<Status> getAll() {
        return statusRepository.findAll();
    }

    @Override
    public List<StatusResponse> getAllWithUseNumber() {
        return statusRepository.findAll().stream()
                .map(status -> new StatusResponse(
                        status.getId(),
                        status.getName(),
                        status.isCloseTicket(),
                        ticketRepository.countByStatusId(status.getId())
                ))
                .toList();
    }

    @Override
    public void update(UpdateStatusRequest request) {
        Status status = statusRepository.findById(request.statusID())
                .orElseThrow(() -> new NotFoundException("Status", request.statusID()));

        if(status.getName().equals(request.name())) {
            throw new NameUnchangedException("Status", request.name());
        }

        if(statusRepository.existsByNameIgnoreCase(request.name())) {
            throw new NameConflictException("Status", request.name());
        }

        status.setName(request.name());
        status.setCloseTicket(request.closeTicket());
        statusRepository.save(status);
    }

    @Override
    public void delete(Long id) {
        Status status = statusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Status", id));

        if(ticketRepository.existsByStatusId(status.getId())) {
            throw new ResourceHasAssignedItemsException("status", "ticket");
        }

        statusRepository.deleteById(status.getId());
    }
}

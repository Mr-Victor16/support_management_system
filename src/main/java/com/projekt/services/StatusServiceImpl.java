package com.projekt.services;

import com.projekt.converter.StatusConverter;
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
    private final StatusConverter statusConverter;

    public StatusServiceImpl(StatusRepository statusRepository, TicketRepository ticketRepository, StatusConverter statusConverter) {
        this.statusRepository = statusRepository;
        this.ticketRepository = ticketRepository;
        this.statusConverter = statusConverter;
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

        // If the new status is marked as default, reset the default flag on all other statuses.
        if(request.defaultStatus()) {
            statusRepository.clearDefaultStatus();
        }

        statusRepository.save(new Status(request.name(), request.closeTicket(), request.defaultStatus()));
    }

    @Override
    public List<Status> getAll() {
        return statusRepository.findAll();
    }

    @Override
    public List<StatusResponse> getAllWithUseNumber() {
        return statusRepository.findAll().stream()
                .map(status -> statusConverter.toStatusResponse(status))
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

        // If the new status is marked as default, reset the default flag on all other statuses.
        if(request.defaultStatus()) {
            statusRepository.clearDefaultStatus();
        }

        status.setDefaultStatus(request.defaultStatus());
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

        if(status.isDefaultStatus()){
            throw DefaultEntityDeletionException.forDefaultStatus();
        }

        statusRepository.deleteById(status.getId());
    }
}

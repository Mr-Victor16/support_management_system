package com.projekt.services;

import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.edit.EditStatusRequest;
import com.projekt.payload.response.StatusResponse;
import com.projekt.repositories.StatusRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        return statusRepository.getReferenceById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return statusRepository.existsById(id);
    }

    @Override
    public void save(AddStatusRequest request) {
        statusRepository.save(new Status(request.getStatusName(), request.isCloseTicket()));
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
                .collect(Collectors.toList());
    }

    @Override
    public void update(EditStatusRequest request) {
        Status status = statusRepository.getReferenceById(request.getStatusId());
        status.setName(request.getStatusName());
        status.setCloseTicket(request.isCloseTicket());

        statusRepository.save(status);
    }

    @Override
    public boolean existsByName(String statusName) {
        return statusRepository.existsByNameIgnoreCase(statusName);
    }

    @Override
    public void delete(Long id) {
        statusRepository.deleteById(id);
    }
}

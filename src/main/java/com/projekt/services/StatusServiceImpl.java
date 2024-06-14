package com.projekt.services;

import com.projekt.models.Status;
import com.projekt.repositories.StatusRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ArrayList<Status> loadAll() {
        return (ArrayList<Status>) statusRepository.findAll();
    }

    @Override
    public Status loadById(Integer id) {
        if(id == null || !statusRepository.existsById(id)){
            return new Status();
        }

        return statusRepository.getReferenceById(id);
    }

    @Override
    public boolean exists(Integer id) {
        return statusRepository.existsById(id);
    }

    @Override
    public void save(Status status) {
        statusRepository.save(status);
    }

    @Override
    public ArrayList<Integer> statusUse() {
        ArrayList<Integer> list = new ArrayList<>();
        List<Status> statuses = statusRepository.findAll();

        for (int i=0; i<statuses.size(); i++){
            list.add(ticketRepository.countByStatus_StatusID(statuses.get(i).getStatusID()));
        }

        return list;
    }

    @Override
    public void delete(Integer id) {
        if(ticketRepository.countByStatus_StatusID(id) == 0 && statusRepository.existsById(id)){
            statusRepository.deleteById(id);
        }
    }

}

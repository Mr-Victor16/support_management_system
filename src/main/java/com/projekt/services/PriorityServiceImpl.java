package com.projekt.services;

import com.projekt.models.Priority;
import com.projekt.repositories.PriorityRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ArrayList<Priority> loadAll() {
        return (ArrayList<Priority>) priorityRepository.findAll();
    }

    @Override
    public Priority loadById(Integer id) {
        if(id == null || !priorityRepository.existsById(id)){
            return new Priority();
        }

        return priorityRepository.getReferenceById(id);
    }

    @Override
    public boolean exists(Integer id) {
        return priorityRepository.existsById(id);
    }

    @Override
    public void save(Priority priority) {
        priorityRepository.save(priority);
    }

    @Override
    public void delete(Integer id) {
        if(ticketRepository.countByPriorityId(id) == 0 && priorityRepository.existsById(id)){
            priorityRepository.deleteById(id);
        }
    }

    @Override
    public ArrayList<Integer> prioritiesUse() {
        ArrayList<Integer> list = new ArrayList<>();
        List<Priority> priorities = priorityRepository.findAll();

        for (int i=0; i<priorities.size(); i++){
            list.add(ticketRepository.countByPriorityId(priorities.get(i).getId()));
        }

        return list;
    }

}

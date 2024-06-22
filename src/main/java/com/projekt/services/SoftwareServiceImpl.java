package com.projekt.services;

import com.projekt.models.Software;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ArrayList<Software> loadAll() {
        return (ArrayList<Software>) softwareRepository.findAll();
    }

    @Override
    public Software loadById(Integer id) {
        if(id == null || !softwareRepository.existsById(Long.valueOf(id))){
            return new Software();
        }

        return softwareRepository.getReferenceById(Long.valueOf(id));
    }

    @Override
    public boolean exists(Integer id) {
        return softwareRepository.existsById(Long.valueOf(id));
    }

    @Override
    public void delete(Integer id) {
        if(ticketRepository.countBySoftwareId(Long.valueOf(id)) == 0 && knowledgeRepository.countBySoftwareId(Long.valueOf(id)) == 0 && softwareRepository.existsById(Long.valueOf(id))){
            softwareRepository.deleteById(Long.valueOf(id));
        }
    }

    @Override
    public void save(Software software) {
        softwareRepository.save(software);
    }

    @Override
    public ArrayList<Integer> softwareUseInTicket() {
        ArrayList<Integer> list = new ArrayList<>();
        List<Software> softwareList = softwareRepository.findAll();

        for (int i=0; i<softwareList.size(); i++){
            list.add((int) ticketRepository.countBySoftwareId(softwareList.get(i).getId()));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInKnowledgeBase() {
        List<Software> softwareList = softwareRepository.findAll();
        ArrayList<Integer> list = new ArrayList<>();

        for (int i=0; i<softwareList.size(); i++){
            list.add((int) knowledgeRepository.countBySoftwareId(softwareList.get(i).getId()));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInTicket(ArrayList<Software> software) {
        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i< ((List<Software>) software).size(); i++){
            list.add((int) ticketRepository.countBySoftwareId(((List<Software>) software).get(i).getId()));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInKnowledgeBase(ArrayList<Software> software) {
        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i< ((List<Software>) software).size(); i++){
            list.add((int) knowledgeRepository.countBySoftwareId(((List<Software>) software).get(i).getId()));
        }

        return list;
    }

    @Override
    public ArrayList<Software> searchSoftwareByNameDescription(String phrase) {
        return softwareRepository.searchSoftwareByNameDescription(phrase);
    }

}

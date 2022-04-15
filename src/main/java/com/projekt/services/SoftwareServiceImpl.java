package com.projekt.services;

import com.projekt.models.Software;
import com.projekt.repositories.SoftwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("softwareDetailsService")
public class SoftwareServiceImpl implements SoftwareService {
    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    public ArrayList<Software> loadAll() {
        return (ArrayList<Software>) softwareRepository.findAll();
    }

    @Override
    public Software loadById(Integer id) {
        if(id == null || softwareRepository.existsById(Long.valueOf(id)) == false){
            return new Software();
        }

        return softwareRepository.getById(Long.valueOf(id));
    }

    @Override
    public boolean exists(Integer id) {
        return softwareRepository.existsById(Long.valueOf(id));
    }

    @Override
    public void delete(Integer id) {
        if(ticketService.countUseSoftware(Long.valueOf(id)) == 0 && knowledgeBaseService.countUseSoftware(id) == 0 && softwareRepository.existsById(Long.valueOf(id))){
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
            list.add(ticketService.countUseSoftware(softwareList.get(i).getSoftwareID()));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInKnowledgeBase() {
        List<Software> softwareList = softwareRepository.findAll();
        ArrayList<Integer> list = new ArrayList<>();

        for (int i=0; i<softwareList.size(); i++){
            list.add(knowledgeBaseService.countUseSoftware(Math.toIntExact(softwareList.get(i).getSoftwareID())));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInTicket(ArrayList<Software> software) {
        ArrayList<Integer> list = new ArrayList<>();
        List<Software> softwareList = software;

        for (int i=0; i<softwareList.size(); i++){
            list.add(ticketService.countUseSoftware(softwareList.get(i).getSoftwareID()));
        }

        return list;
    }

    @Override
    public ArrayList<Integer> softwareUseInKnowledgeBase(ArrayList<Software> software) {
        List<Software> softwareList = software;
        ArrayList<Integer> list = new ArrayList<>();

        for (int i=0; i<softwareList.size(); i++){
            list.add(knowledgeBaseService.countUseSoftware(Math.toIntExact(softwareList.get(i).getSoftwareID())));
        }

        return list;
    }

    @Override
    public ArrayList<Software> searchSoftwareByNameDescription(String phrase) {
        return softwareRepository.searchSoftwareByNameDescription(phrase);
    }


}

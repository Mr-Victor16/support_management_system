package com.projekt.services;

import com.projekt.models.Software;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface SoftwareService {
    ArrayList<Software> loadAll();

    Software loadById(Integer id);

    boolean exists(Integer id);

    void delete(Integer id);

    void save(Software software);

    ArrayList<Integer> softwareUseInTicket();

    ArrayList<Integer> softwareUseInKnowledgeBase();

    ArrayList<Integer> softwareUseInTicket(ArrayList<Software> software);

    ArrayList<Integer> softwareUseInKnowledgeBase(ArrayList<Software> software);

    ArrayList<Software> searchSoftwareByNameDescription(String phrase);

}

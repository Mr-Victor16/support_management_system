package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
public interface KnowledgeBaseService{
    ArrayList<Knowledge> loadAll();

    Knowledge loadById(Integer id);

    void save(Knowledge knowledge);

    void delete(Integer id);

    boolean exists(Integer id);

    int countUseSoftware(Integer id);

    ArrayList<Knowledge> searchKnowledgeByTitleContent(String phrase);

    ArrayList<Knowledge> searchKnowledgeByDate(LocalDate date1, LocalDate date2);

    ArrayList<Knowledge> searchKnowledgeBySoftware(Software software);
}

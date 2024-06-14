package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service("knowledgeBaseDetailsService")
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService{
    private final KnowledgeRepository knowledgeRepository;
    private final SoftwareRepository softwareRepository;

    public KnowledgeBaseServiceImpl(KnowledgeRepository knowledgeRepository, SoftwareRepository softwareRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.softwareRepository = softwareRepository;
    }

    @Override
    public ArrayList<Knowledge> loadAll() {
        return (ArrayList<Knowledge>) knowledgeRepository.findAll();
    }

    @Override
    public Knowledge loadById(Integer id) {
        if(id == null || !knowledgeRepository.existsById(id)){
            return new Knowledge();
        }

        return knowledgeRepository.getReferenceById(id);
    }

    @Override
    public void save(Knowledge knowledge) {
        knowledge.setSoftware(softwareRepository.getReferenceById(knowledge.getSoftware().getId()));
        knowledgeRepository.save(knowledge);
    }

    @Override
    public void delete(Integer id) {
        if(knowledgeRepository.existsById(id)){
            knowledgeRepository.deleteById(id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        return knowledgeRepository.existsById(id);
    }

    @Override
    public ArrayList<Knowledge> searchKnowledgeByTitleContent(String phrase) {
        return knowledgeRepository.searchKnowledgeByTitleContent(phrase);
    }

    @Override
    public ArrayList<Knowledge> searchKnowledgeByDate(LocalDate date1, LocalDate date2) {
        return knowledgeRepository.searchKnowledgeByDate(date1, date2);
    }

    @Override
    public ArrayList<Knowledge> searchKnowledgeBySoftware(Software software) {
        return knowledgeRepository.searchKnowledgeBySoftware(software.getId());
    }

}

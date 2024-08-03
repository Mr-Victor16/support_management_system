package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.payload.request.AddKnowledgeRequest;
import com.projekt.payload.request.EditKnowledgeRequest;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service("knowledgeBaseDetailsService")
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService{
    private final KnowledgeRepository knowledgeRepository;
    private final SoftwareRepository softwareRepository;

    public KnowledgeBaseServiceImpl(KnowledgeRepository knowledgeRepository, SoftwareRepository softwareRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.softwareRepository = softwareRepository;
    }

    @Override
    public List<Knowledge> getAll() {
        return knowledgeRepository.findAll();
    }

    @Override
    public Knowledge loadById(Long id) {
        return knowledgeRepository.getReferenceById(id);
    }

    @Override
    public void save(AddKnowledgeRequest request) {
        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(request.getTitle());
        knowledge.setContent(request.getContent());
        knowledge.setDate(request.getDate());
        knowledge.setSoftware(softwareRepository.getReferenceById(knowledge.getSoftware().getId()));

        knowledgeRepository.save(knowledge);
    }

    @Override
    public void delete(Long id) {
        knowledgeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return knowledgeRepository.existsById(id);
    }

    @Override
    public boolean findDuplicate(String knowledgeTitle, Long softwareID) {
        Knowledge knowledge = knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle);
        if (knowledge == null) return false;
        else return Objects.equals(knowledge.getSoftware().getId(), softwareID);
    }

    @Override
    public void update(EditKnowledgeRequest request) {
        Knowledge knowledge = knowledgeRepository.getReferenceById(request.getKnowledgeId());
        knowledge.setTitle(request.getTitle());
        knowledge.setContent(request.getContent());
        knowledge.setDate(request.getDate());
        knowledge.setSoftware(softwareRepository.getReferenceById(request.getSoftwareId()));
        knowledgeRepository.save(knowledge);
    }
}

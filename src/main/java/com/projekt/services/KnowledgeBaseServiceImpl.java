package com.projekt.services;

import com.projekt.exceptions.KnowledgeConflictException;
import com.projekt.exceptions.NotFoundException;
import com.projekt.models.Knowledge;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
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
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Knowledge", id));
    }

    @Override
    public void add(AddKnowledgeRequest request) {
        if(findDuplicate(request.title(), request.softwareID())) {
            throw new KnowledgeConflictException(request.title(), request.softwareID());
        }

        Knowledge knowledge = new Knowledge(
                request.title(),
                request.content(),
                softwareRepository.findById(request.softwareID())
                        .orElseThrow(() -> new NotFoundException("Software", request.softwareID()))
        );

        knowledgeRepository.save(knowledge);
    }

    @Override
    public void delete(Long id) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Knowledge", id));

        knowledgeRepository.deleteById(knowledge.getId());
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
    public void update(UpdateKnowledgeRequest request) {
        Knowledge knowledge = knowledgeRepository.findById(request.knowledgeID())
                .orElseThrow(() -> new NotFoundException("Knowledge", request.knowledgeID()));

        if(findDuplicate(request.title(), request.softwareID())) {
            throw new KnowledgeConflictException(request.title(), request.softwareID());
        }

        knowledge.setTitle(request.title());
        knowledge.setContent(request.content());
        knowledge.setSoftware(softwareRepository.findById(request.softwareID())
                .orElseThrow(() -> new NotFoundException("Software", request.softwareID())));
        knowledgeRepository.save(knowledge);
    }
}

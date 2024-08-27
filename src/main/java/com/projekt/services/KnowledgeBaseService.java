package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface KnowledgeBaseService{
    List<Knowledge> getAll();

    Knowledge loadById(Long id);

    void save(AddKnowledgeRequest knowledgeRequest);

    void delete(Long id);

    boolean existsById(Long id);

    boolean findDuplicate(String knowledgeTitle, Long softwareID);

    void update(UpdateKnowledgeRequest knowledgeRequest);

    boolean existsBySoftwareId(Long softwareID);
}

package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;

import java.util.List;

public interface KnowledgeBaseService{
    List<Knowledge> getAll();

    Knowledge loadById(Long id);

    void add(AddKnowledgeRequest request);

    void delete(Long id);

    boolean existsById(Long id);

    boolean findDuplicate(String title, Long softwareID);

    void update(UpdateKnowledgeRequest request);
}

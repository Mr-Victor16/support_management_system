package com.projekt.controllers;

import com.projekt.models.Knowledge;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import com.projekt.services.KnowledgeBaseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {
    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public List<Knowledge> getAllKnowledgeItems(){
        return knowledgeBaseService.getAll();
    }

    @GetMapping("{knowledgeID}")
    public Knowledge getKnowledgeById(@PathVariable(name = "knowledgeID") Long knowledgeID){
        return knowledgeBaseService.loadById(knowledgeID);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateKnowledge(@RequestBody @Valid UpdateKnowledgeRequest request){
        knowledgeBaseService.update(request);
        return "Knowledge updated";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String addKnowledge(@RequestBody @Valid AddKnowledgeRequest request){
        knowledgeBaseService.add(request);
        return "Knowledge added";
    }

    @DeleteMapping("{knowledgeID}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteKnowledge(@PathVariable(name = "knowledgeID") Long knowledgeID){
        knowledgeBaseService.delete(knowledgeID);
        return "Knowledge removed";
    }
}

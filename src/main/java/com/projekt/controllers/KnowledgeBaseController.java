package com.projekt.controllers;

import com.projekt.models.Knowledge;
import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import com.projekt.services.KnowledgeBaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Knowledge>> getAllKnowledgeItems(){
        return ResponseEntity.ok(knowledgeBaseService.getAll());
    }

    @GetMapping("{knowledgeID}")
    public ResponseEntity<Knowledge> getKnowledgeById(@PathVariable(name = "knowledgeID") Long knowledgeID){
        return ResponseEntity.ok(knowledgeBaseService.loadById(knowledgeID));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateKnowledge(@RequestBody @Valid UpdateKnowledgeRequest request){
        knowledgeBaseService.update(request);
        return new ResponseEntity<>("Knowledge updated", HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addKnowledge(@RequestBody @Valid AddKnowledgeRequest request){
        knowledgeBaseService.add(request);
        return new ResponseEntity<>("Knowledge added", HttpStatus.OK);
    }

    @DeleteMapping("{knowledgeID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteKnowledge(@PathVariable(name = "knowledgeID") Long knowledgeID){
        knowledgeBaseService.delete(knowledgeID);
        return new ResponseEntity<>("Knowledge removed successfully", HttpStatus.OK);
    }
}

package com.projekt.controllers;

import com.projekt.payload.request.add.AddKnowledgeRequest;
import com.projekt.payload.request.edit.EditKnowledgeRequest;
import com.projekt.services.KnowledgeBaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {
    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ResponseEntity<?> getAllKnowledgeItems(){
        return ResponseEntity.ok(knowledgeBaseService.getAll());
    }

    @GetMapping("{knowledgeID}")
    public ResponseEntity<?> getKnowledgeById(@PathVariable(name = "knowledgeID") Long knowledgeID){
        if(knowledgeBaseService.existsById(knowledgeID)){
            return ResponseEntity.ok(knowledgeBaseService.loadById(knowledgeID));
        }

        return new ResponseEntity<>("No knowledge found", HttpStatus.NOT_FOUND);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editKnowledge(@RequestBody @Valid EditKnowledgeRequest request){
        if(!knowledgeBaseService.existsById(request.getKnowledgeID())) {
            return new ResponseEntity<>("No knowledge found", HttpStatus.NOT_FOUND);
        }

        if(!knowledgeBaseService.loadById(request.getKnowledgeID()).getTitle().equalsIgnoreCase(request.getTitle())){
            if(knowledgeBaseService.findDuplicate(request.getTitle(), request.getSoftwareID())){
                return new ResponseEntity<>("Knowledge already exists", HttpStatus.CONFLICT);
            }
        }

        knowledgeBaseService.update(request);
        return new ResponseEntity<>("Knowledge edited", HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addKnowledge(@RequestBody @Valid AddKnowledgeRequest request){
        if(!knowledgeBaseService.findDuplicate(request.getTitle(), request.getSoftwareID())){
            knowledgeBaseService.save(request);
            return new ResponseEntity<>("Knowledge added", HttpStatus.OK);
        }

        return new ResponseEntity<>("Knowledge already exists", HttpStatus.CONFLICT);
    }

    @DeleteMapping("{knowledgeID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteKnowledge(@PathVariable(name = "knowledgeID") Long knowledgeID){
        if(knowledgeBaseService.existsById(knowledgeID)){
            knowledgeBaseService.delete(knowledgeID);
            return new ResponseEntity<>("Knowledge removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("No knowledge found", HttpStatus.NOT_FOUND);
    }
}

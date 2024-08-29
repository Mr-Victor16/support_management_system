package com.projekt.controllers;

import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.services.PriorityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/priorities")
public class PriorityController {
    private final PriorityService priorityService;

    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<List<Priority>> getAllPriorities(){
        return ResponseEntity.ok(priorityService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<PriorityResponse>> getAllPrioritiesWithUseNumber(){
        return ResponseEntity.ok(priorityService.getAllWithUseNumber());
    }

    @GetMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Priority> getPriorityById(@PathVariable(name = "priorityID") Long priorityID){
        return ResponseEntity.ok(priorityService.loadById(priorityID));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePriority(@RequestBody @Valid UpdatePriorityRequest request){
        priorityService.update(request);
        return new ResponseEntity<>("Priority updated", HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addPriority(@RequestBody @Valid AddPriorityRequest request){
        priorityService.add(request);
        return new ResponseEntity<>("Priority added", HttpStatus.OK);
    }

    @DeleteMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePriority(@PathVariable(name = "priorityID") Long priorityID){
        priorityService.delete(priorityID);
        return new ResponseEntity<>("Priority removed successfully", HttpStatus.OK);
    }
}

package com.projekt.controllers;

import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import com.projekt.services.PriorityService;
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
    public List<Priority> getAllPriorities(){
        return priorityService.getAll();
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public List<PriorityResponse> getAllPrioritiesWithUseNumber(){
        return priorityService.getAllWithUseNumber();
    }

    @GetMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public Priority getPriorityById(@PathVariable(name = "priorityID") Long priorityID){
        return priorityService.loadById(priorityID);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePriority(@RequestBody @Valid UpdatePriorityRequest request){
        priorityService.update(request);
        return "Priority updated";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String addPriority(@RequestBody @Valid AddPriorityRequest request){
        priorityService.add(request);
        return "Priority added";
    }

    @DeleteMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletePriority(@PathVariable(name = "priorityID") Long priorityID){
        priorityService.delete(priorityID);
        return "Priority removed";
    }
}

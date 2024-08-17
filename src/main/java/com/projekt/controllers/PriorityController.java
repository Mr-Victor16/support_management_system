package com.projekt.controllers;

import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.edit.EditPriorityRequest;
import com.projekt.repositories.TicketRepository;
import com.projekt.services.PriorityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/priority")
public class PriorityController {
    private final PriorityService priorityService;
    private final TicketRepository ticketRepository;

    public PriorityController(PriorityService priorityService, TicketRepository ticketRepository) {
        this.priorityService = priorityService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllPriorities(){
        return ResponseEntity.ok(priorityService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllPrioritiesWithUseNumber(){
        return ResponseEntity.ok(priorityService.getAllWithUseNumber());
    }

    @GetMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPriorityById(@PathVariable(name = "priorityID") Long priorityID){
        if(priorityService.existsById(priorityID)){
            return ResponseEntity.ok(priorityService.loadById(priorityID));
        }

        return new ResponseEntity<>("No priority found", HttpStatus.NOT_FOUND);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editPriority(@RequestBody @Valid EditPriorityRequest request){
        if(!priorityService.existsById(request.getPriorityID())){
            return new ResponseEntity<>("No priority found", HttpStatus.NOT_FOUND);
        }

        if(priorityService.loadById(request.getPriorityID()).getName().equals(request.getName())){
            return new ResponseEntity<>("Priority name is the same as the current name", HttpStatus.OK);
        }

        if(!priorityService.existsByName(request.getName())){
            priorityService.update(request);
            return new ResponseEntity<>("Priority name edited", HttpStatus.OK);
        }

        return new ResponseEntity<>("Priority already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPriority(@RequestBody @Valid AddPriorityRequest request){
        if(!priorityService.existsByName(request.getName())){
            priorityService.save(request);
            return new ResponseEntity<>("Priority added", HttpStatus.OK);
        }

        return new ResponseEntity<>("Priority already exists", HttpStatus.CONFLICT);
    }

    @DeleteMapping("{priorityID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePriority(@PathVariable(name = "priorityID") Long priorityID){
        if(!priorityService.existsById(priorityID)){
            return new ResponseEntity<>("No priority found", HttpStatus.NOT_FOUND);
        }

        if(!ticketRepository.existsByPriorityId(priorityID)){
            priorityService.delete(priorityID);
            return new ResponseEntity<>("Priority removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a priority if it has a ticket assigned to it", HttpStatus.CONFLICT);
    }
}

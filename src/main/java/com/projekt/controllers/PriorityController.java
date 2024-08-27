package com.projekt.controllers;

import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.update.UpdatePriorityRequest;
import com.projekt.services.PriorityService;
import com.projekt.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/priorities")
public class PriorityController {
    private final PriorityService priorityService;
    private final TicketService ticketService;

    public PriorityController(PriorityService priorityService, TicketService ticketService) {
        this.priorityService = priorityService;
        this.ticketService = ticketService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
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
    public ResponseEntity<?> updatePriority(@RequestBody @Valid UpdatePriorityRequest request){
        if(!priorityService.existsById(request.priorityID())){
            return new ResponseEntity<>("No priority found", HttpStatus.NOT_FOUND);
        }

        if(priorityService.loadById(request.priorityID()).getName().equals(request.name())){
            return new ResponseEntity<>("Priority name is the same as the current name", HttpStatus.OK);
        }

        if(!priorityService.existsByName(request.name())){
            priorityService.update(request);
            return new ResponseEntity<>("Priority name updated", HttpStatus.OK);
        }

        return new ResponseEntity<>("Priority already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPriority(@RequestBody @Valid AddPriorityRequest request){
        if(!priorityService.existsByName(request.name())){
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

        if(!ticketService.existsByPriorityId(priorityID)){
            priorityService.delete(priorityID);
            return new ResponseEntity<>("Priority removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a priority if it has a ticket assigned to it", HttpStatus.CONFLICT);
    }
}

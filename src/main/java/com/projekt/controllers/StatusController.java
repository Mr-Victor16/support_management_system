package com.projekt.controllers;

import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.edit.EditStatusRequest;
import com.projekt.services.StatusService;
import com.projekt.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/status")
public class StatusController {
    private final StatusService statusService;
    private final TicketService ticketService;

    public StatusController(StatusService statusService, TicketService ticketService) {
        this.statusService = statusService;
        this.ticketService = ticketService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllStatuses(){
        return ResponseEntity.ok(statusService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllStatusesWithUseNumber(){
        return ResponseEntity.ok(statusService.getAllWithUseNumber());
    }

    @GetMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatusById(@PathVariable(name = "statusID") Long statusID){
        if(statusService.existsById(statusID)){
            return ResponseEntity.ok(statusService.loadById(statusID));
        }

        return new ResponseEntity<>("No status found", HttpStatus.NOT_FOUND);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editStatus(@RequestBody @Valid EditStatusRequest request){
        if(!statusService.existsById(request.getStatusID())){
            return new ResponseEntity<>("No status found", HttpStatus.NOT_FOUND);
        }

        if(statusService.loadById(request.getStatusID()).getName().equals(request.getName())){
            return new ResponseEntity<>("Status name is the same as the current name", HttpStatus.OK);
        }

        if(!statusService.existsByName(request.getName())){
            statusService.update(request);
            return new ResponseEntity<>("Status name edited", HttpStatus.OK);
        }

        return new ResponseEntity<>("Status already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addStatus(@RequestBody @Valid AddStatusRequest request){
        if(!statusService.existsByName(request.getName())){
            statusService.save(request);
            return new ResponseEntity<>("Status added", HttpStatus.OK);
        }

        return new ResponseEntity<>("Status already exists", HttpStatus.CONFLICT);
    }

    @DeleteMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStatus(@PathVariable(name = "statusID") Long statusID){
        if(!statusService.existsById(statusID)){
            return new ResponseEntity<>("No status found", HttpStatus.NOT_FOUND);
        }

        if(!ticketService.existsByStatusId(statusID)){
            statusService.delete(statusID);
            return new ResponseEntity<>("Status removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a status if it has a ticket assigned to it", HttpStatus.CONFLICT);
    }
}

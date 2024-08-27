package com.projekt.controllers;

import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.services.KnowledgeBaseService;
import com.projekt.services.SoftwareService;
import com.projekt.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/software")
public class SoftwareController {
    private final SoftwareService softwareService;
    private final TicketService ticketService;
    private final KnowledgeBaseService knowledgeBaseService;

    public SoftwareController(SoftwareService softwareService, TicketService ticketService, KnowledgeBaseService knowledgeBaseService) {
        this.softwareService = softwareService;
        this.ticketService = ticketService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ResponseEntity<?> getAllSoftware() {
        return ResponseEntity.ok(softwareService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllSoftwareWithUseNumbers(){
        return ResponseEntity.ok(softwareService.getAllWithUseNumber());
    }

    @GetMapping("{softwareID}")
    public ResponseEntity<?> getSoftwareById(@PathVariable(name = "softwareID") Long softwareID){
        if(softwareService.existsById(softwareID)){
            return ResponseEntity.ok(softwareService.loadById(softwareID));
        }

        return new ResponseEntity<>("No software found", HttpStatus.NOT_FOUND);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSoftware(@RequestBody @Valid UpdateSoftwareRequest request){
        if(!softwareService.existsById(request.softwareID())){
            return new ResponseEntity<>("No software found", HttpStatus.NOT_FOUND);
        }

        if(softwareService.loadById(request.softwareID()).getName().equals(request.name())){
            return new ResponseEntity<>("Software name is the same as the current name", HttpStatus.OK);
        }

        if(!softwareService.existsByName(request.name())){
            softwareService.update(request);
            return new ResponseEntity<>("Software details updated", HttpStatus.OK);
        }

        return new ResponseEntity<>("Software already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSoftware(@RequestBody @Valid AddSoftwareRequest request){
        if(!softwareService.existsByName(request.name())){
            softwareService.save(request);
            return new ResponseEntity<>("Software added", HttpStatus.OK);
        }

        return new ResponseEntity<>("Software already exists", HttpStatus.CONFLICT);
    }

    @DeleteMapping("{softwareID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSoftware(@PathVariable(name = "softwareID") Long softwareID){
        if(!softwareService.existsById(softwareID)){
            return new ResponseEntity<>("No software found", HttpStatus.NOT_FOUND);
        }

        if(!ticketService.existsBySoftwareId(softwareID) && !knowledgeBaseService.existsBySoftwareId(softwareID)){
            softwareService.delete(softwareID);
            return new ResponseEntity<>("Software removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a software if it has a ticket or knowledge assigned to it", HttpStatus.CONFLICT);
    }
}

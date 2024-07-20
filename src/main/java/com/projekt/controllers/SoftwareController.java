package com.projekt.controllers;

import com.projekt.payload.request.AddSoftwareRequest;
import com.projekt.payload.request.EditSoftwareRequest;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.TicketRepository;
import com.projekt.services.SoftwareService;
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
    private final KnowledgeRepository knowledgeRepository;
    private final TicketRepository ticketRepository;

    public SoftwareController(SoftwareService softwareService, KnowledgeRepository knowledgeRepository, TicketRepository ticketRepository) {
        this.softwareService = softwareService;
        this.knowledgeRepository = knowledgeRepository;
        this.ticketRepository = ticketRepository;
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
    public ResponseEntity<?> editSoftware(@RequestBody @Valid EditSoftwareRequest request){
        if(!softwareService.existsById(request.getSoftwareId())){
            return new ResponseEntity<>("No software found", HttpStatus.NOT_FOUND);
        }

        if(softwareService.loadById(request.getSoftwareId()).getName().equals(request.getSoftwareName())){
            return new ResponseEntity<>("Software name is the same as the current name", HttpStatus.OK);
        }

        if(!softwareService.existsByName(request.getSoftwareName())){
            softwareService.update(request);
            return new ResponseEntity<>("Software details edited", HttpStatus.OK);
        }

        return new ResponseEntity<>("Software already exists", HttpStatus.CONFLICT);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSoftware(@RequestBody @Valid AddSoftwareRequest request){
        if(!softwareService.existsByName(request.getSoftwareName())){
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

        if(!ticketRepository.existsBySoftwareId(softwareID) && !knowledgeRepository.existsBySoftwareId(softwareID)){
            softwareService.delete(softwareID);
            return new ResponseEntity<>("Software removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You cannot remove a software if it has a ticket or knowledge assigned to it", HttpStatus.CONFLICT);
    }
}

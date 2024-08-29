package com.projekt.controllers;

import com.projekt.models.Software;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;
import com.projekt.services.SoftwareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/software")
public class SoftwareController {
    private final SoftwareService softwareService;

    public SoftwareController(SoftwareService softwareService) {
        this.softwareService = softwareService;
    }

    @GetMapping
    public ResponseEntity<List<Software>> getAllSoftware() {
        return ResponseEntity.ok(softwareService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<SoftwareResponse>> getAllSoftwareWithUseNumbers(){
        return ResponseEntity.ok(softwareService.getAllWithUseNumber());
    }

    @GetMapping("{softwareID}")
    public ResponseEntity<Software> getSoftwareById(@PathVariable(name = "softwareID") Long softwareID){
        return ResponseEntity.ok(softwareService.loadById(softwareID));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSoftware(@RequestBody @Valid UpdateSoftwareRequest request){
        softwareService.update(request);
        return new ResponseEntity<>("Software details updated", HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addSoftware(@RequestBody @Valid AddSoftwareRequest request){
        softwareService.add(request);
        return new ResponseEntity<>("Software added", HttpStatus.OK);
    }

    @DeleteMapping("{softwareID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSoftware(@PathVariable(name = "softwareID") Long softwareID){
        softwareService.delete(softwareID);
        return new ResponseEntity<>("Software removed successfully", HttpStatus.OK);
    }
}

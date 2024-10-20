package com.projekt.controllers;

import com.projekt.models.Software;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;
import com.projekt.services.SoftwareService;
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
    public List<Software> getAllSoftware() {
        return softwareService.getAll();
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public List<SoftwareResponse> getAllSoftwareWithUseNumbers(){
        return softwareService.getAllWithUseNumber();
    }

    @GetMapping("{softwareID}")
    public Software getSoftwareById(@PathVariable(name = "softwareID") Long softwareID){
        return softwareService.loadById(softwareID);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateSoftware(@RequestBody @Valid UpdateSoftwareRequest request){
        softwareService.update(request);
        return "Software details updated";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String addSoftware(@RequestBody @Valid AddSoftwareRequest request){
        softwareService.add(request);
        return "Software added";
    }

    @DeleteMapping("{softwareID}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSoftware(@PathVariable(name = "softwareID") Long softwareID){
        softwareService.delete(softwareID);
        return "Software removed";
    }
}

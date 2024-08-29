package com.projekt.controllers;

import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;
import com.projekt.services.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
public class StatusController {
    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<Status>> getAllStatuses(){
        return ResponseEntity.ok(statusService.getAll());
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<StatusResponse>> getAllStatusesWithUseNumber(){
        return ResponseEntity.ok(statusService.getAllWithUseNumber());
    }

    @GetMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Status> getStatusById(@PathVariable(name = "statusID") Long statusID){
        return ResponseEntity.ok(statusService.loadById(statusID));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStatus(@RequestBody @Valid UpdateStatusRequest request){
        statusService.update(request);
        return new ResponseEntity<>("Status edited", HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addStatus(@RequestBody @Valid AddStatusRequest request){
        statusService.add(request);
        return new ResponseEntity<>("Status added", HttpStatus.OK);
    }

    @DeleteMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteStatus(@PathVariable(name = "statusID") Long statusID){
        statusService.delete(statusID);
        return new ResponseEntity<>("Status removed successfully", HttpStatus.OK);
    }
}

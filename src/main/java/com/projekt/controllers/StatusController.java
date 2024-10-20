package com.projekt.controllers;

import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;
import com.projekt.services.StatusService;
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
    public List<Status> getAllStatuses(){
        return statusService.getAll();
    }

    @GetMapping("/use")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public List<StatusResponse> getAllStatusesWithUseNumber(){
        return statusService.getAllWithUseNumber();
    }

    @GetMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public Status getStatusById(@PathVariable(name = "statusID") Long statusID){
        return statusService.loadById(statusID);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(@RequestBody @Valid UpdateStatusRequest request){
        statusService.update(request);
        return "Status details updated";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String addStatus(@RequestBody @Valid AddStatusRequest request){
        statusService.add(request);
        return "Status added";
    }

    @DeleteMapping("{statusID}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStatus(@PathVariable(name = "statusID") Long statusID){
        statusService.delete(statusID);
        return "Status remove";
    }
}

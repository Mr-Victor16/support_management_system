package com.projekt.controllers;

import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.response.TicketResponse;
import com.projekt.services.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketReplyService ticketReplyService;
    private final ImageService imageService;

    public TicketController(TicketService ticketService, TicketReplyService ticketReplyService, ImageService imageService) {
        this.ticketService = ticketService;
        this.ticketReplyService = ticketReplyService;
        this.imageService = imageService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public List<TicketResponse> getAllTickets(){
        return ticketService.getAll();
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public List<TicketResponse> getUserTickets(Principal principal){
        return ticketService.getUserTickets(principal);
    }

    @GetMapping("/user/{userID}")
    @PreAuthorize("hasRole('OPERATOR')")
    public List<TicketResponse> getTicketsByUserId(@PathVariable(name = "userID", required = false) Long userID){
        return ticketService.getUserTickets(userID);
    }

    @GetMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public TicketResponse getTicketById(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        return ticketService.getById(ticketID, principal);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public String addTicket(@RequestBody @Valid AddTicketRequest request, Principal principal){
        ticketService.add(request, principal.getName());
        return "Ticket added";
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public String updateTicket(@RequestBody @Valid UpdateTicketRequest request, Principal principal) {
        ticketService.update(request, principal);
        return "Ticket details updated";
    }

    @PostMapping("{ticketID}/image")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public String addImages(@PathVariable("ticketID") Long ticketID, @RequestBody List<MultipartFile> files, Principal principal) {
        imageService.add(ticketID, files, principal);
        return "Image added";
    }

    @DeleteMapping("/image/{imageID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public String deleteImage(@PathVariable(name = "imageID", required = false) Long imageID, Principal principal) {
        imageService.deleteById(imageID, principal);
        return "Image removed";
    }

    @PostMapping("/reply")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public String addTicketReply(@RequestBody @Valid AddTicketReplyRequest request, Principal principal) {
        ticketReplyService.add(request, principal);
        return "Ticket reply added";
    }

    @PostMapping("/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public String changeTicketStatus(@RequestBody @Valid UpdateTicketStatusRequest request) {
        ticketService.changeStatus(request.ticketID(), request.statusID());
        return "Ticket status changed";
    }

    @Transactional
    @DeleteMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR')")
    public String deleteTicket(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        ticketService.delete(ticketID, principal);
        return "Ticket removed";
    }

    @DeleteMapping("/reply/{replyID}")
    @PreAuthorize("hasRole('OPERATOR')")
    public String deleteTicketReply(@PathVariable(name = "replyID", required = false) Long replyID) {
        ticketReplyService.deleteById(replyID);
        return "Ticket reply removed";
    }
}

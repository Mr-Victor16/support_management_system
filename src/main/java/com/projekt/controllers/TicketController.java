package com.projekt.controllers;

import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.payload.response.TicketResponse;
import com.projekt.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getAllTickets(){
        return ResponseEntity.ok(ticketService.getAll());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getUserTickets(Principal principal){
        return ResponseEntity.ok(ticketService.getUserTickets(principal));
    }

    @GetMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        return ResponseEntity.ok(ticketService.getById(ticketID, principal));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> addTicket(@RequestBody @Valid AddTicketRequest request, Principal principal){
        ticketService.add(request, principal.getName());
        return new ResponseEntity<>("Ticket added", HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> updateTicket(@RequestBody @Valid UpdateTicketRequest request, Principal principal) {
        ticketService.update(request, principal);
        return ResponseEntity.ok("Ticket details changed successfully");
    }

    @PostMapping("{ticketID}/image")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> addImages(@PathVariable("ticketID") Long ticketID, @RequestBody List<MultipartFile> files, Principal principal) {
        imageService.add(ticketID, files, principal);
        return ResponseEntity.ok("Image added successfully");
    }

    @DeleteMapping("/image/{imageID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> deleteImage(@PathVariable(name = "imageID", required = false) Long imageID, Principal principal) {
        imageService.deleteById(imageID, principal);
        return new ResponseEntity<>("Image removed successfully", HttpStatus.OK);
    }

    @PostMapping("/reply")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> addTicketReply(@RequestBody @Valid AddTicketReplyRequest request, Principal principal) {
        ticketReplyService.add(request, principal);
        return ResponseEntity.ok("Ticket reply added successfully");
    }

    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<String> changeTicketStatus(@RequestBody @Valid UpdateTicketStatusRequest request) {
        ticketService.changeStatus(request.ticketID(), request.statusID());
        return ResponseEntity.ok("Ticket status changed successfully");
    }

    @Transactional
    @DeleteMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> deleteTicket(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        ticketService.delete(ticketID, principal);
        return new ResponseEntity<>("Ticket removed successfully", HttpStatus.OK);
    }

    @DeleteMapping("/reply/{replyID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<String> deleteTicketReply(@PathVariable(name = "replyID", required = false) Long replyID) {
        ticketReplyService.deleteById(replyID);
        return new ResponseEntity<>("Ticket reply removed successfully", HttpStatus.OK);
    }
}

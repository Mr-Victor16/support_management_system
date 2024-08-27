package com.projekt.controllers;

import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.add.AddTicketReply;
import com.projekt.payload.request.update.UpdateTicketStatusRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketReplyService ticketReplyService;
    private final ImageService imageService;
    private final StatusService statusService;
    private final UserService userService;

    public TicketController(TicketService ticketService, TicketReplyService ticketReplyService, ImageService imageService, StatusService statusService, UserService userService) {
        this.ticketService = ticketService;
        this.ticketReplyService = ticketReplyService;
        this.imageService = imageService;
        this.statusService = statusService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllTickets(){
        return ResponseEntity.ok(ticketService.getAll());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getUserTickets(Principal principal){
        return ResponseEntity.ok(userService.findUserByUsername(principal.getName()).getTickets());
    }

    @GetMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getTicketById(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        if(!ticketService.existsById(ticketID)){
            return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
        }

        if(ticketService.isAuthorized(ticketID, principal.getName())){
            return ResponseEntity.ok(ticketService.getById(ticketID));
        }

        return new ResponseEntity<>("You don't have access rights to this resource", HttpStatus.FORBIDDEN);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> addTicket(@RequestBody @Valid AddTicketRequest request, Principal principal){
        if(!ticketService.entitiesExist(request.categoryID(), request.statusID(), request.priorityID(), request.softwareID())){
            return new ResponseEntity<>("One or more entities do not exist", HttpStatus.NOT_FOUND);
        }

        ticketService.add(request, principal.getName());
        return new ResponseEntity<>("Ticket added", HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> updateTicket(@RequestBody @Valid UpdateTicketRequest request, Principal principal) {
        if(!ticketService.existsById(request.ticketID())){
            return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
        }

        if(ticketService.isAuthorized(request.ticketID(), principal.getName())){
            ticketService.update(request);
            return ResponseEntity.ok("Ticket details changed successfully");
        }

        return new ResponseEntity<>("You have no rights to update this resource", HttpStatus.FORBIDDEN);
    }

    @PostMapping("{ticketID}/image")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> addImage(@PathVariable("ticketID") Long ticketID, @RequestBody MultipartFile file, Principal principal) {
        try {
            if (!ticketService.existsById(ticketID)) {
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }

            if (ticketService.isAuthorized(ticketID, principal.getName())) {
                ticketService.addImage(ticketID, file);
                return ResponseEntity.ok("Image added successfully");
            }

            return new ResponseEntity<>("You have no rights to add image to this ticket", HttpStatus.FORBIDDEN);
        } catch (IOException e) {
            return new ResponseEntity<>("Error occurred while processing the image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/image/{imageID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> deleteImage(@PathVariable(name = "imageID", required = false) Long imageID, Principal principal) {
        if(!imageService.existsById(imageID)){
            return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
        }

        if(ticketService.isAuthorized(ticketService.findByImageId(imageID).getId(), principal.getName())){
            imageService.deleteById(imageID);
            return new ResponseEntity<>("Image removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You have no rights to delete this resource", HttpStatus.FORBIDDEN);
    }

    @PostMapping("/reply")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> addTicketReply(@RequestBody @Valid AddTicketReply request, Principal principal) {
        try {
            if(!ticketService.existsById(request.ticketID())){
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }

            if(ticketService.isAuthorized(request.ticketID(), principal.getName())){
                ticketService.addReply(request);
                return ResponseEntity.ok("Ticket reply added successfully");
            }

            return new ResponseEntity<>("You have no rights to add reply to this ticket", HttpStatus.FORBIDDEN);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error occurred while sending notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> changeTicketStatus(@RequestBody @Valid UpdateTicketStatusRequest request) {
        try {
            if (!ticketService.existsById(request.ticketID())) {
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }

            if (!statusService.existsById(request.statusID())) {
                return new ResponseEntity<>("Status not found", HttpStatus.NOT_FOUND);
            }

            ticketService.changeStatus(request.ticketID(), request.statusID());
            return ResponseEntity.ok("Ticket status changed successfully");
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error occurred while sending notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @DeleteMapping("{ticketID}")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> deleteTicket(@PathVariable(name = "ticketID", required = false) Long ticketID, Principal principal) {
        if(ticketService.isAuthorized(ticketID,principal.getName())){
            ticketService.delete(ticketID);
            return new ResponseEntity<>("Ticket removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("You have no rights to delete this resource", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/reply/{replyID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> deleteReply(@PathVariable(name = "replyID", required = false) Long replyID) {
        if(ticketReplyService.existsById(replyID)){
            ticketReplyService.deleteById(replyID);
            return new ResponseEntity<>("Ticket reply removed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("Ticket reply not found", HttpStatus.NOT_FOUND);
    }
}

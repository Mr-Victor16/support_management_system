package com.projekt.controllers;

import com.projekt.repositories.UserRepository;
import com.projekt.services.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketsController {
    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketsController(TicketService ticketService,
                             UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllTickets(){
        return ResponseEntity.ok(ticketService.getAll());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getUserTickets(Principal principal){
        Long userID = userRepository.findByUsernameIgnoreCase(principal.getName()).getId();

        return ResponseEntity.ok(ticketService.getTicketsByUserId(userID));
    }
}

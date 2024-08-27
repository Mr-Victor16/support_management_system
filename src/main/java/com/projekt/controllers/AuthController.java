package com.projekt.controllers;

import com.projekt.payload.request.LoginRequest;
import com.projekt.payload.request.RegisterRequest;
import com.projekt.services.UserServiceImpl;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserServiceImpl userService;

    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        if(userService.existsByUsername(request.username())) {
            return ResponseEntity.ok(userService.authenticate(request));
        }

        return new ResponseEntity<>("Incorrect login details", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if(userService.existsByUsername(request.username())) {
            return new ResponseEntity<>("Username already in use", HttpStatus.CONFLICT);
        }

        if(userService.existsByEmail(request.email())) {
            return new ResponseEntity<>("Email address already in use", HttpStatus.CONFLICT);
        }

        try {
            userService.register(request);
            return new ResponseEntity<>("User added", HttpStatus.OK);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error occurred while sending notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.projekt.controllers;

import com.projekt.payload.request.LoginRequest;
import com.projekt.payload.request.RegisterRequest;
import com.projekt.payload.response.LoginResponse;
import com.projekt.services.UserServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserServiceImpl userService;

    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse authenticateUser(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return "User added";
    }
}

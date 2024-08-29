package com.projekt.controllers;

import com.projekt.payload.request.update.UpdateProfileDetailsRequest;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<UserDetailsResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(userService.getUserDetails(principal.getName()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<String> updateProfile(Principal principal, @RequestBody @Valid UpdateProfileDetailsRequest request) {
        userService.updateProfile(principal.getName(), request);
        return new ResponseEntity<>("Profile updated", HttpStatus.OK);
    }

    @GetMapping("/activate/{userID}")
    public ResponseEntity<String> activateProfile(@PathVariable Long userID) {
        userService.activate(userID);
        return new ResponseEntity<>("User activated", HttpStatus.OK);
    }
}

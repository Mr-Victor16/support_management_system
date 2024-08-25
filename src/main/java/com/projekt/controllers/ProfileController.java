package com.projekt.controllers;

import com.projekt.payload.request.edit.EditProfileDetailsRequest;
import com.projekt.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getProfile(Principal principal) {
        if(!userService.existsByUsername(principal.getName())){
            return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(userService.getUserDetails(principal.getName()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<?> updateProfile(Principal principal, @RequestBody @Valid EditProfileDetailsRequest request) {
        if (!userService.existsByUsername(principal.getName())){
            return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
        }

        userService.updateProfile(principal.getName(), request);
        return new ResponseEntity<>("Profile updated", HttpStatus.OK);
    }

    @GetMapping("/activate/{userID}")
    public ResponseEntity<?> activateProfile(@PathVariable Long userID) {
        if(!userService.existsById(userID)){
            return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
        }

        if(userService.isActive(userID)){
            return new ResponseEntity<>("User already activated", HttpStatus.CONFLICT);
        }

        userService.activate(userID);
        return new ResponseEntity<>("User activated", HttpStatus.OK);
    }
}

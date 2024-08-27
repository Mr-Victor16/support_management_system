package com.projekt.controllers;

import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{userID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable(name = "userID", required = false) Long userID){
        if (!userService.existsById(userID)) {
            return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(userService.loadById(userID));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody @Valid AddUserRequest request){
        if (userService.existsByUsername(request.username()) || userService.existsByEmail(request.email())){
            return new ResponseEntity<>("Username or Email already exists", HttpStatus.CONFLICT);
        }

        userService.addUser(request);
        return new ResponseEntity<>("User added", HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UpdateUserRequest request){
        if (!userService.existsById(request.userID())) {
            return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
        }

        try {
            userService.editUser(request);
            return new ResponseEntity<>("User edited", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Username or Email already exists", HttpStatus.CONFLICT);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> getAllUsers(){
        return ResponseEntity.ok(userService.loadAll());
    }

    @DeleteMapping("{userID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("userID") Long userID){
        if(userID != 1) {
            if (userService.existsById(userID)) {
                userService.delete(userID);
                return new ResponseEntity<>("User removed successfully", HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>("Default administrator account cannot be deleted", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND);
    }
}

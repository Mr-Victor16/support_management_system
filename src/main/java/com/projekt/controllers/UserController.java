package com.projekt.controllers;

import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{userID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<UserDetailsResponse> getUser(@PathVariable(name = "userID", required = false) Long userID){
        return ResponseEntity.ok(userService.loadById(userID));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<String> addUser(@RequestBody @Valid AddUserRequest request){
        userService.add(request);
        return new ResponseEntity<>("User added", HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<String> updateUser(@RequestBody @Valid UpdateUserRequest request){
        userService.updateUser(request);
        return new ResponseEntity<>("User edited", HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<UserDetailsResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.loadAll());
    }

    @DeleteMapping("{userID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable("userID") Long userID){
        userService.delete(userID);
        return new ResponseEntity<>("User removed successfully", HttpStatus.OK);
    }
}

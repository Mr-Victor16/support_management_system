package com.projekt.controllers;

import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.services.UserService;
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
    public UserDetailsResponse getUser(@PathVariable(name = "userID", required = false) Long userID){
        return userService.loadById(userID);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public String addUser(@RequestBody @Valid AddUserRequest request){
        userService.add(request);
        return "User added";
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public String updateUser(@RequestBody @Valid UpdateUserRequest request){
        userService.updateUser(request);
        return "User edited";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public List<UserDetailsResponse> getAllUsers(){
        return userService.loadAll();
    }

    @DeleteMapping("{userID}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public String deleteUser(@PathVariable("userID") Long userID){
        userService.delete(userID);
        return "User removed";
    }
}

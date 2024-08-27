package com.projekt.services;

import com.projekt.models.User;
import com.projekt.payload.request.*;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateProfileDetailsRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.payload.response.LoginResponse;
import com.projekt.payload.response.UserDetailsResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.mail.MessagingException;

import java.util.List;

public interface UserService extends UserDetailsService {
    User findUserByUsername(String name);

    List<UserDetailsResponse> loadAll();

    boolean existsById(Long id);

    UserDetailsResponse loadById(Long id);

    void editUser(UpdateUserRequest request) throws Exception;

    void delete(Long id);

    void activate(Long userID);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void register(RegisterRequest request) throws MessagingException;

    LoginResponse authenticate(LoginRequest request);

    UserDetailsResponse getUserDetails(String name);

    void updateProfile(String username, UpdateProfileDetailsRequest request);

    boolean isActive(Long userID);

    void addUser(AddUserRequest request);
}

package com.projekt.services;

import com.projekt.payload.request.*;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateProfileDetailsRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.payload.response.LoginResponse;
import com.projekt.payload.response.UserDetailsResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends UserDetailsService {
    List<UserDetailsResponse> loadAll();

    boolean existsById(Long id);

    UserDetailsResponse loadById(Long id);

    void updateUser(UpdateUserRequest request);

    void delete(Long id);

    void activate(Long id);

    void register(RegisterRequest request);

    LoginResponse authenticate(LoginRequest request);

    UserDetailsResponse getUserDetails(String username);

    void updateProfile(String username, UpdateProfileDetailsRequest request);

    void add(AddUserRequest request);
}

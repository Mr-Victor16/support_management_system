package com.projekt.payload.response;

import com.projekt.models.Role;

public record UserDetailsResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        Role role
) { }

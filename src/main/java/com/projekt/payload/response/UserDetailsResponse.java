package com.projekt.payload.response;

public record UserDetailsResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String role
) { }

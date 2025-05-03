package com.projekt.payload.response;

public record LoginResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String token,
        String role,
        String type
) {
    public LoginResponse(Long id, String username, String name, String surname, String email, String token, String role) {
        this(id, username, name, surname, email, token, role, "Bearer");
    }
}

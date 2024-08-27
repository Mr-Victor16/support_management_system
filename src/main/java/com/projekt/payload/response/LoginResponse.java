package com.projekt.payload.response;

import java.util.List;

public record LoginResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String token,
        List<String>roles,
        String type
) {
    public LoginResponse(Long id, String username, String name, String surname, String email, String token, List<String> roles) {
        this(id, username, name, surname, email, token, roles, "Bearer");
    }
}

package com.projekt.payload.response;

import java.util.List;

public record UserDetailsResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        List<String>roles
) { }

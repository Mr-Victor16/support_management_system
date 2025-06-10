package com.projekt.payload.request.update;

public record UpdateProfileDetailsRequest(
        String name,
        String surname,
        String password
) { }

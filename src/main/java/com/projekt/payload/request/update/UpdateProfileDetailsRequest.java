package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileDetailsRequest(
        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotBlank
        String password
) { }

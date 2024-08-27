package com.projekt.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 2, max = 36)
        @NotBlank
        String username,

        @NotBlank
        @JsonIgnore
        String password,

        @Email
        @NotBlank
        String email,

        @Size(min = 2, max = 30)
        @NotBlank
        @FirstCharacterConstraint
        String name,

        @Size(min = 2, max = 60)
        @NotBlank
        @FirstCharacterConstraint
        String surname
) { }

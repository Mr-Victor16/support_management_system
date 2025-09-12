package com.projekt.payload.request.update;

import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @Positive
        Long userID,

        @Size(min = 2, max = 36)
        @NotBlank
        String username,

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
        String surname,

        @NotBlank
        String role
) { }

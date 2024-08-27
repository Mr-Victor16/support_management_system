package com.projekt.payload.request.update;

import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.*;

import java.util.List;

public record UpdateUserRequest(
        @NotNull
        @Positive
        Long userID,

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

        @NotNull
        Boolean enabled,

        @NotNull
        @NotEmpty
        List<String> roles
) { }

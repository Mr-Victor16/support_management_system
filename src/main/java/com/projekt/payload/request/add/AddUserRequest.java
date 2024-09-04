package com.projekt.payload.request.add;

import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.*;

import java.util.List;

public record AddUserRequest(
        @Size(min = 2, max = 36)
        @NotBlank
        String username,

        @NotBlank
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
        String surname,

        @NotEmpty
        List<String> roles
) { }

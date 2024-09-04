package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSoftwareRequest(
        @Positive
        Long softwareID,

        @Size(min = 2, max = 30)
        @NotBlank
        String name,

        @Size(min = 10, max = 200)
        @NotBlank
        String description
) { }

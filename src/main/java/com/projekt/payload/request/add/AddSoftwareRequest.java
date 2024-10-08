package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSoftwareRequest(
        @Size(min = 2, max = 30)
        @NotBlank
        String name,

        @Size(min = 10, max = 200)
        @NotBlank
        String description
) { }

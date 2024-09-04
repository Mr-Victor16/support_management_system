package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Positive
        Long categoryID,

        @Size(min = 2, max = 20)
        @NotBlank
        String name
) { }

package com.projekt.payload.request.update;

import jakarta.validation.constraints.*;

public record UpdatePriorityRequest(
        @Positive
        Long priorityID,

        @Size(min = 3, max = 20)
        @NotBlank
        String name
) { }

package com.projekt.payload.request.update;

import jakarta.validation.constraints.*;

public record UpdatePriorityRequest(
        @NotNull
        @Positive
        Long priorityID,

        @Size(min = 3, max = 20)
        @NotBlank
        String name,

        @Min(1)
        @NotNull
        Integer maxTime
) { }

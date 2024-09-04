package com.projekt.payload.request.add;

import jakarta.validation.constraints.*;

public record AddTicketRequest(
        @Size(min = 5, max = 100)
        @NotBlank
        String title,

        @Size(min = 5, max = 500)
        @NotBlank
        String description,

        @Positive
        Long categoryID,

        @Positive
        Long priorityID,

        @Size(min = 1, max = 10)
        @NotBlank
        String version,

        @Positive
        Long softwareID
) { }

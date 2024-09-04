package com.projekt.payload.request.update;

import jakarta.validation.constraints.*;

public record UpdateTicketRequest(
        @Positive
        Long ticketID,

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

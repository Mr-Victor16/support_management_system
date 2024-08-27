package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTicketStatusRequest(
        @NotNull
        @Positive
        Long ticketID,

        @NotNull
        @Positive
        Long statusID
) { }

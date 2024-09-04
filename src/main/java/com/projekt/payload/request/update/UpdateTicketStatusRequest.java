package com.projekt.payload.request.update;

import jakarta.validation.constraints.Positive;

public record UpdateTicketStatusRequest(
        @Positive
        Long ticketID,

        @Positive
        Long statusID
) { }

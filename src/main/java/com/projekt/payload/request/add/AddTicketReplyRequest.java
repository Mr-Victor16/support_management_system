package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddTicketReplyRequest(
        @Positive
        Long ticketID,

        @Size(min = 5, max = 500)
        @NotBlank
        String content
) { }

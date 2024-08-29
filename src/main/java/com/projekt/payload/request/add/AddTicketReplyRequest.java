package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AddTicketReplyRequest(
        @NotNull
        @Positive
        Long ticketID,

        @NotNull
        @Positive
        Long userID,

        @Size(min = 5, max = 500)
        @NotBlank
        String content,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate date
) { }

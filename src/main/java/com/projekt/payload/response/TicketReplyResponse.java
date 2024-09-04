package com.projekt.payload.response;

import java.time.LocalDate;

public record TicketReplyResponse(
        Long id,
        UserDetailsResponse user,
        String content,
        LocalDate createdDate
) { }

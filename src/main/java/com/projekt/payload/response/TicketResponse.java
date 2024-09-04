package com.projekt.payload.response;

import com.projekt.models.*;

import java.time.LocalDate;
import java.util.List;

public record TicketResponse(
        Long id,
        String title,
        String description,
        List<Image> images,
        LocalDate createdDate,
        Category category,
        Priority priority,
        Status status,
        String version,
        Software software,
        List<TicketReplyResponse> replies,
        UserDetailsResponse user
) { }

package com.projekt.payload.response;

public record StatusResponse(
        Long statusID,
        String name,
        boolean closeTicket,
        boolean defaultStatus,
        Long useNumber
) { }

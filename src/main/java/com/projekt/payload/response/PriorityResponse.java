package com.projekt.payload.response;

public record PriorityResponse(
        Long priorityID,
        String name,
        Long useNumber
) { }

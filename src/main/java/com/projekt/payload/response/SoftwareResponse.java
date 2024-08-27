package com.projekt.payload.response;

public record SoftwareResponse(
        Long softwareID,
        String name,
        String description,
        Long useNumberTicket,
        Long useNumberKnowledge
) { }

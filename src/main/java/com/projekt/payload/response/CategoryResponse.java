package com.projekt.payload.response;

public record CategoryResponse(
        Long categoryID,
        String name,
        Long useNumber
) { }

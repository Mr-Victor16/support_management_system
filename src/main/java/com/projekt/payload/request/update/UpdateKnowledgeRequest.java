package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateKnowledgeRequest(
        @Positive
        Long knowledgeID,

        @Size(min = 2, max = 50)
        @NotBlank
        String title,

        @Size(min = 20, max = 360)
        @NotBlank
        String content,

        @Positive
        Long softwareID
) { }

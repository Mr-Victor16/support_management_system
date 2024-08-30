package com.projekt.payload.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateKnowledgeRequest(
        @NotNull
        @Positive
        Long knowledgeID,

        @Size(min = 2, max = 50)
        @NotBlank
        String title,

        @Size(min = 20, max = 360)
        @NotBlank
        String content,

        @NotNull
        @Positive
        Long softwareID
) { }

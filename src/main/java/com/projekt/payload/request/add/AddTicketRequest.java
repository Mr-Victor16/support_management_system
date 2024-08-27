package com.projekt.payload.request.add;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record AddTicketRequest(
        @Size(min = 5, max = 100)
        @NotBlank
        String title,

        @Size(min = 5, max = 500)
        @NotBlank
        String description,

        @NotNull
        @NotEmpty
        List<MultipartFile> multipartFiles,

        @NotNull
        @Positive
        Long categoryID,

        @NotNull
        @Positive
        Long priorityID,

        @NotNull
        @Positive
        Long statusID,

        @Size(min = 1, max = 10)
        @NotBlank
        String version,

        @NotNull
        @Positive
        Long softwareID
) { }

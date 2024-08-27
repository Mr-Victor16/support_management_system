package com.projekt.payload.request.update;

import com.projekt.models.Image;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public record UpdateTicketRequest(
        @NotNull
        @Positive
        Long ticketID,

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
        @NotEmpty
        List<Image> images,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate date,

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

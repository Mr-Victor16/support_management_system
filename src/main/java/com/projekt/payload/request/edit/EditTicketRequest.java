package com.projekt.payload.request.edit;

import com.projekt.models.Image;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditTicketRequest {
    @NotNull
    @Positive
    private Long ticketID;

    @Size(min = 5, max = 100)
    @NotBlank
    private String title;

    @Size(min = 5, max = 500)
    @NotBlank
    private String description;

    @NotNull
    @NotEmpty
    private List<MultipartFile> multipartFiles = new ArrayList<>();

    @NotNull
    @NotEmpty
    private List<Image> images = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate date;

    @NotNull
    @Positive
    private Long categoryID;

    @NotNull
    @Positive
    private Long priorityID;

    @NotNull
    @Positive
    private Long statusID;

    @Size(min = 1, max = 10)
    @NotBlank
    private String version;

    @NotNull
    @Positive
    private Long softwareID;
}

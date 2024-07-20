package com.projekt.payload.request;

import com.projekt.models.Image;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Long ticketID;

    @Size(min = 5, max = 100)
    @NotBlank
    private String title;

    @Size(min = 5, max = 500)
    @NotBlank
    private String description;

    private List<MultipartFile> multipartFiles = new ArrayList<>();
    private List<Image> images = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull
    private Long categoryID;

    @NotNull
    private Long priorityID;

    @NotNull
    private Long statusID;

    @Size(min = 1, max = 10)
    @NotBlank
    private String version;

    @NotNull
    private Long softwareID;
}


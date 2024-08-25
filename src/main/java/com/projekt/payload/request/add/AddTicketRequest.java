package com.projekt.payload.request.add;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddTicketRequest {
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

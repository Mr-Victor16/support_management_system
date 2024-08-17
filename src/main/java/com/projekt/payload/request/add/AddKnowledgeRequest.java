package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddKnowledgeRequest {
    @Size(min = 2, max = 50)
    @NotBlank
    private String title;

    @Size(min = 20, max = 360)
    @NotBlank
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate date;

    @NotNull
    private Long softwareID;
}

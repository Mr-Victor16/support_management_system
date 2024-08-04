package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddSoftwareRequest {
    @Size(min = 2, max = 30)
    @NotBlank
    private String softwareName;

    @Size(min = 10, max = 200)
    @NotBlank
    private String description;
}

package com.projekt.payload.request.add;

import jakarta.validation.constraints.Min;
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
public class AddPriorityRequest {
    @Size(min = 3, max = 20)
    @NotBlank
    private String name;

    @Min(1)
    private Integer maxTime;
}

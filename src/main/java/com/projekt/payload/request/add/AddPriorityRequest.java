package com.projekt.payload.request.add;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddPriorityRequest(
        @Size(min = 3, max = 20)
        @NotBlank
        String name,

        @Min(1)
        Integer maxTime
) { }

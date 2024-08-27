package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddStatusRequest(
        @Size(min = 2, max = 20)
        @NotBlank
        String name,

        @NotNull
        Boolean closeTicket
) { }

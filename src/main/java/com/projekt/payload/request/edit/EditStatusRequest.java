package com.projekt.payload.request.edit;

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
public class EditStatusRequest {
    private Long statusId;

    @Size(min = 2, max = 20)
    @NotBlank
    private String statusName;

    private boolean closeTicket;
}

package com.projekt.payload.request.edit;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditTicketStatusRequest {
    @NotNull
    private Long ticketID;

    @NotNull
    private Long statusID;
}

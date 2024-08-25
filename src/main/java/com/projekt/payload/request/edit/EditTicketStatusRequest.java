package com.projekt.payload.request.edit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private Long ticketID;

    @NotNull
    @Positive
    private Long statusID;
}

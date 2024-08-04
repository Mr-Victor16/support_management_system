package com.projekt.payload.request.edit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditTicketStatusRequest {
    private Long ticketID;
    private Long statusID;
}

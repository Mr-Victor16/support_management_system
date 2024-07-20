package com.projekt.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatusResponse {
    private Long statusId;
    private String statusName;
    private boolean closeTicket;
    private Long useNumber;
}

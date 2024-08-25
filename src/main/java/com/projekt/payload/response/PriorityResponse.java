package com.projekt.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PriorityResponse {
    private Long priorityID;
    private String name;
    private Long useNumber;
}

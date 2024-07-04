package com.projekt.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SoftwareResponse {
    private Long id;
    private String name;
    private String description;
    private Long useNumberTicket;
    private Long useNumberKnowledge;
}

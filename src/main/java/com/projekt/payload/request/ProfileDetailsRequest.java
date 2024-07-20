package com.projekt.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileDetailsRequest {
    private Long id;
    private String name;
    private String surname;
}

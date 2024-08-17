package com.projekt.payload.request.edit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EditProfileDetailsRequest {
    private String name;
    private String surname;
    private String password;
}

package com.projekt.payload.request.edit;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EditProfileDetailsRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    private String password;
}

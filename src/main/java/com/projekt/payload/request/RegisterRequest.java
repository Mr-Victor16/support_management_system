package com.projekt.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest {
    @Size(min = 2, max = 36)
    @NotBlank
    private String username;

    @NotBlank
    @JsonIgnore
    private String password;

    @Email
    @NotBlank
    private String email;

    @Size(min = 2, max = 30)
    @NotBlank
    @FirstCharacterConstraint
    private String name;

    @Size(min = 2, max = 60)
    @NotBlank
    @FirstCharacterConstraint
    private String surname;
}

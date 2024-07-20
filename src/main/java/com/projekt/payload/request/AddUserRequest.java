package com.projekt.payload.request;

import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddUserRequest {
    @Size(min = 2, max = 36)
    @NotBlank
    private String username;

    @NotEmpty
    @NotBlank
    private String password;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    @Size(min = 2, max = 30)
    @NotBlank
    @FirstCharacterConstraint
    private String name;

    @NotEmpty
    @Size(min = 2, max = 60)
    @NotBlank
    @FirstCharacterConstraint
    private String surname;

    private List<String> roles;
}

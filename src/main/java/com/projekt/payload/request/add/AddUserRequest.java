package com.projekt.payload.request.add;

import com.projekt.validators.FirstCharacterConstraint;
import jakarta.validation.constraints.*;
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

    @NotBlank
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

    @NotNull
    @NotEmpty
    private List<String> roles;
}

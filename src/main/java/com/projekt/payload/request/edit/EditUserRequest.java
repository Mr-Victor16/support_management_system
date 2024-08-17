package com.projekt.payload.request.edit;

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
public class EditUserRequest {
    @NotNull
    private Long id;

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

    private boolean enabled;

    private List<String> roles;
}

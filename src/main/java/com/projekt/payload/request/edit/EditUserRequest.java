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
    @Positive
    private Long id;

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
    private Boolean enabled;

    @NotNull
    @NotEmpty
    private List<String> roles;
}

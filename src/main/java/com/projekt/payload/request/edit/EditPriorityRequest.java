package com.projekt.payload.request.edit;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditPriorityRequest {
    @NotNull
    @Positive
    private Long priorityID;

    @Size(min = 3, max = 20)
    @NotBlank
    private String Name;

    @Min(1)
    @NotNull
    private Integer maxTime;
}

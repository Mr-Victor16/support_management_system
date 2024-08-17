package com.projekt.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FirstCharacterValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstCharacterConstraint {
    String message() default "Name must start with a capital letter";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] values() default {};
}

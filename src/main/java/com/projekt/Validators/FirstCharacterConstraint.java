package com.projekt.Validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FirstCharacterValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstCharacterConstraint {
    String message() default "Nazwa powinna rozpoczynać się od dużej litery";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] values() default {};
}

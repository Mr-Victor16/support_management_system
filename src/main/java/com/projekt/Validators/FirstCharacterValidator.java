package com.projekt.Validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FirstCharacterValidator implements ConstraintValidator<FirstCharacterConstraint, String> {
    public FirstCharacterConstraint constraint;

    @Override
    public void initialize(FirstCharacterConstraint constraintAnnotation) {
        this.constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if(name != null){
            return Character.isUpperCase(name.charAt(0));
        }

        return false;
    }
}

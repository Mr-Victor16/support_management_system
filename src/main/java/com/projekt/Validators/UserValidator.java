package com.projekt.Validators;

import com.projekt.models.User;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var user = (User) target;
        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            errors.rejectValue("password", "RozneHasla");
            errors.rejectValue("passwordConfirm", "RozneHasla");
        }

        if (user.getPasswordConfirm().equals("")){
            errors.rejectValue("passwordConfirm", "PustePole");
        }
    }
}

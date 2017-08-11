package de.otto.prototype.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SecurePasswordValidator implements ConstraintValidator<SecurePasswordConstraint, String> {

    @Override
    public void initialize(SecurePasswordConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null /*&& password.matches("[0-9]+")*/
                && (password.length() > 7) && (password.length() < 17);
    }
}

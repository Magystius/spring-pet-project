package de.otto.prototype.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SecurePasswordValidator implements ConstraintValidator<SecurePassword, String> {

	private String pattern;

	@Override
	public void initialize(SecurePassword constraintAnnotation) {
		this.pattern = constraintAnnotation.pattern();
	}

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		return password != null && !password.isEmpty() && password.matches(pattern)
				&& (password.length() > 7) && (password.length() < 17);
	}
}

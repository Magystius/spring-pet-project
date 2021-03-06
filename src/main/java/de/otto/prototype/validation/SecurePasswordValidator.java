package de.otto.prototype.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SecurePasswordValidator implements ConstraintValidator<SecurePassword, String> {

	private String pattern;

	@Override
	public void initialize(final SecurePassword constraintAnnotation) {
		this.pattern = constraintAnnotation.pattern();
	}

	@Override
	public boolean isValid(final String password, final ConstraintValidatorContext context) {
		return password != null && !password.isEmpty() && password.matches(pattern)
				&& (password.length() > 7) && (password.length() < 17);
	}
}

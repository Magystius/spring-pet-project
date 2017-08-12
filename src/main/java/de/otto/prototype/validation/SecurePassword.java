package de.otto.prototype.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = SecurePasswordValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface SecurePassword {
	String message() default "error.password";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}

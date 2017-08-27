package de.otto.prototype.validation;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SecurePasswordValidatorTest {

	private SecurePasswordValidator testee;

	@BeforeEach
	void setup() {
		testee = new SecurePasswordValidator();
		testee.initialize(implementAnnotationInterface("[0-9]+"));
	}

	private SecurePassword implementAnnotationInterface(String pattern) {
		AnnotationDescriptor<SecurePassword> descriptor = new AnnotationDescriptor<>(SecurePassword.class);
		descriptor.setValue("pattern", pattern);
		return AnnotationFactory.create(descriptor);
	}

	@ParameterizedTest
	@ValueSource(strings = {"111", "11111111111111111111111111111111", "invalid", ""})
	@DisplayName("should return false for every unsecure password")
	void shouldValidateInsecurePasswords(String insecurePassword) {
		assertThat(testee.isValid(insecurePassword, null), is(false));
	}

	@Test
	@DisplayName("should return false for null password")
	void shouldValidateNullPassword() {
		assertThat(testee.isValid(null, null), is(false));
	}

	@Test
	@DisplayName("should return true for a secure password")
	void shouldValidateSecurePassword() {
		testee.initialize(implementAnnotationInterface(".*"));
		assertThat(testee.isValid("securePassword", null), is(true));
	}
}

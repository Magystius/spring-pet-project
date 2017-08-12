package de.otto.prototype.validation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SecurePasswordValidatorTest {

	private LocalValidatorFactoryBean localValidatorFactory;

	@Before
	public void setup() {
		//TODO: could we use autowired here?
		localValidatorFactory = new LocalValidatorFactoryBean();
		localValidatorFactory.setProviderClass(HibernateValidator.class);
		localValidatorFactory.afterPropertiesSet();
	}

	@Test
	public void shouldValidateSecurePassword() {
		Password passwordToValidate = new Password("securePassword");
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(0));
	}

	@Test
	public void shouldValidateTooShortPassword() {
		Password passwordToValidate = new Password("short");
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

	@Test
	public void shouldValidateTooLongPassword() {
		Password passwordToValidate = new Password("loooooooooooooooong");
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

	@Test
	public void shouldValidateEmptyPassword() {
		Password passwordToValidate = new Password("");
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

	private class Password {
		@SecurePassword(pattern = ".*")
		String password;

		Password(String password) {
			this.password = password;
		}
	}
}

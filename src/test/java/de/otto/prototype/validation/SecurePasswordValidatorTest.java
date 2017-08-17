package de.otto.prototype.validation;

import de.otto.prototype.model.Password;
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
		Password passwordToValidate = Password.builder().password("securePassword").build();
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(0));
	}

	@Test
	public void shouldValidateTooShortPassword() {
		Password passwordToValidate = Password.builder().password("short").build();
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

	@Test
	public void shouldValidateTooLongPassword() {
		Password passwordToValidate = Password.builder().password("loooooooooooooooong").build();
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

	@Test
	public void shouldValidateEmptyPassword() {
		Password passwordToValidate = Password.builder().password("").build();
		Set<ConstraintViolation<Password>> constraintViolations = localValidatorFactory.validate(passwordToValidate);
		assertThat(constraintViolations.size(), is(1));
		assertThat(constraintViolations.iterator().next().getMessage(), is("error.password"));
	}

}

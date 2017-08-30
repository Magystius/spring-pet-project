package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

class PasswordServiceTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private PasswordService testee;

	@BeforeEach
	void setUp() throws Exception {
		initMocks(this);
		LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
		validatorFactory.setProviderClass(HibernateValidator.class);
		validatorFactory.afterPropertiesSet();

		testee = new PasswordService(userService, validatorFactory);
	}

	@Nested
	@DisplayName("when a new password for a user id is send")
	class updatePassword {
		@Test
		@DisplayName("should update the password and return the updated user")
		void shouldReturnUpdatedUser() throws Exception {
			final String userId = "someId";
			final String password = "somePassword";
			final User userToUpdate = User.builder().id(userId).lastName("Mustermann").login(Login.builder().build()).build();
			final User updatedUser = User.builder().id(userId).lastName("Mustermann").login(Login.builder().password(password).build()).build();
			given(userService.findOne(userId)).willReturn(of(userToUpdate));
			given(userService.update(updatedUser, null)).willReturn(updatedUser);

			final User persistedUser = testee.update(userId, password);
			assertAll("user",
					() -> assertThat(persistedUser.getLogin().getPassword(), is(password)),
					() -> assertThat(persistedUser.getId(), is(userId)));
			then(userService).should(times(1)).findOne(userId);
			then(userService).should(times(1)).update(updatedUser, null);
			then(userService).shouldHaveNoMoreInteractions();
			System.out.println(mockingDetails(userService).printInvocations());
		}

		@Test
		@DisplayName("should throw a not found exception if id for user is null")
		void shouldReturnNotFoundExceptionIfIdIsNull() throws Exception {
			NotFoundException exception =
					assertThrows(NotFoundException.class, () -> testee.update(null, "somePassword"));
			assertThat(exception.getMessage(), is("user not found"));
			then(userService).shouldHaveZeroInteractions();
			System.out.println(mockingDetails(userService).printInvocations());
		}

		@Test
		@DisplayName("should throw a not found exception if the given id can´t be found")
		void shouldReturnNotFoundExceptionIfUnknownId() throws Exception {
			final String userId = "someId";
			given(userService.findOne(userId)).willReturn(empty());
			NotFoundException exception =
					assertThrows(NotFoundException.class, () -> testee.update(userId, "somePassword"));
			assertThat(exception.getMessage(), is("user not found"));
			then(userService).should(times(1)).findOne(userId);
			then(userService).shouldHaveNoMoreInteractions();
			System.out.println(mockingDetails(userService).printInvocations());
		}
	}

	@Nested
	@DisplayName("when a string is tested if it´s a secure password")
	class checkPassword {
		@Test
		@DisplayName("should return false for a unsecure password")
		void shouldReturnFalseForInsecurePassword() {
			Boolean result = testee.checkPassword("unsec");
			assertThat(result, is(false));
		}

		@Test
		@DisplayName("should return true for a secure password")
		void shouldReturnTrueForSecurePassword() {
			Boolean result = testee.checkPassword("securePassword");
			assertThat(result, is(true));
		}
	}
}
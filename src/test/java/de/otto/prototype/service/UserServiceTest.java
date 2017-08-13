package de.otto.prototype.service;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private static final User validMinimumUser =
			User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();

	private static final Long validUserId = 1234L;

	@Mock
	private UserRepository userRepository;

	private Validator validator;

	private UserService testee;

	@Before
	public void setUp() throws Exception {
		LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
		validatorFactory.setProviderClass(HibernateValidator.class);
		validatorFactory.afterPropertiesSet();
		validator = validatorFactory;

		testee = new UserService(userRepository, validator);
	}

	@Test
	public void shouldReturnEmptyListIfNoUserIsFound() throws Exception {
		when(userRepository.streamAll()).thenReturn(Stream.of());

		final Stream<User> returnedList = testee.findAll();

		assertThat(returnedList.collect(toList()).size(), is(0));
	}

	@Test
	public void shouldReturnListOfUsersFound() throws Exception {
		final User userToReturn = User.builder().lastName("Mustermann").build();
		when(userRepository.streamAll()).thenReturn(Stream.of(userToReturn));

		final List<User> listOfReturnedUser = testee.findAll().collect(toList());

		final Supplier<Stream<User>> sup = listOfReturnedUser::stream;
		assertThat(sup.get().collect(toList()).size(), is(1));
		assertThat(sup.get().collect(toList()).get(0), is(userToReturn));
		verify(userRepository, times(1)).streamAll();
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	public void shouldReturnAUserIfFound() throws Exception {
		Long userId = 1234L;
		String userLastName = "Mustermann";
		final User userToReturn = User.builder().id(userId).lastName(userLastName).build();
		when(userRepository.findOne(userId)).thenReturn(userToReturn);

		final User foundUser = testee.findOne(userId).orElse(null);

		assert foundUser != null;
		assertThat(foundUser.getId(), is(userId));
		assertThat(foundUser.getLastName(), is(userLastName));
		verify(userRepository, times(1)).findOne(userId);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	public void shouldReturnNoUserIfNotFound() throws Exception {
		Long userId = 1234L;
		when(userRepository.findOne(userId)).thenReturn(null);

		final Optional<User> foundUser = testee.findOne(userId);

		assertThat(foundUser.isPresent(), is(false));
		verify(userRepository, times(1)).findOne(userId);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	public void shouldReturnCreatedUser() throws Exception {
		User persistedUser = validMinimumUser.toBuilder().id(1234L).build();
		when(userRepository.save(validMinimumUser)).thenReturn(persistedUser);

		final User returendUser = testee.create(validMinimumUser);

		assertThat(returendUser, is(persistedUser));
		verify(userRepository, times(1)).save(validMinimumUser);
		verifyNoMoreInteractions(userRepository);
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldThrowConstraintViolationExceptionIfInvalidNewUser() {
		try {
			testee.create(validMinimumUser.toBuilder().firstName("a").build());
		} catch (ConstraintViolationException e) {
			String msgCode = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}

	@Test(expected = InvalidUserException.class)
	public void shouldThrowInvalidUserExceptionOnNewUserWithWrongMail() {
		User invalidUserToCreate = validMinimumUser.toBuilder().mail("max.mustermann@web.de").build();

		try {
			testee.create(invalidUserToCreate);
		} catch (InvalidUserException e) {
			assertThat(e.getUser(), is(invalidUserToCreate));
			assertThat(e.getErrorMsg(), is("only mails by otto allowed"));
			assertThat(e.getErrorCause(), is("business"));
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}

	@Test
	public void shouldReturnUpdatedUser() throws Exception {
		final User updatedUser = validMinimumUser.toBuilder().id(validUserId).lastName("Neumann").build();
		when(userRepository.findOne(validUserId)).thenReturn(validMinimumUser);
		when(userRepository.save(updatedUser)).thenReturn(updatedUser);

		final User persistedUser = testee.update(updatedUser);

		assertThat(persistedUser.getLastName(), is("Neumann"));
		assertThat(persistedUser.getId(), is(validUserId));
		verify(userRepository, times(1)).findOne(validUserId);
		verify(userRepository, times(1)).save(updatedUser);
		verifyNoMoreInteractions(userRepository);
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldThrowConstraintViolationExceptionIfInvalidExistingUser() {
		User invalidUserToUpdate = validMinimumUser.toBuilder().id(validUserId).firstName("a").build();
		when(userRepository.findOne(validUserId)).thenReturn(invalidUserToUpdate);

		try {
			testee.update(invalidUserToUpdate);
		} catch (ConstraintViolationException e) {
			String msgCode = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			throw e;
		}
	}

	@Test(expected = InvalidUserException.class)
	public void shouldThrowInvalidUserExceptionOnExistingUserWithWrongMail() {
		User invalidUserToUpdate = validMinimumUser.toBuilder().id(validUserId).mail("max.mustermann@web.de").build();
		when(userRepository.findOne(validUserId)).thenReturn(invalidUserToUpdate);

		try {
			testee.update(invalidUserToUpdate);
		} catch (InvalidUserException e) {
			assertThat(e.getUser(), is(invalidUserToUpdate));
			assertThat(e.getErrorMsg(), is("only mails by otto allowed"));
			assertThat(e.getErrorCause(), is("business"));
			throw e;
		}
	}

	@Test(expected = NotFoundException.class)
	public void shouldReturnNotFoundExceptionIfIdUnknown() throws Exception {
		when(userRepository.findOne(validUserId)).thenReturn(null);

		try {
			testee.update(validMinimumUser.toBuilder().id(validUserId).build());
		} catch (InvalidUserException e) {
			assertThat(e.getMessage(), is("id not found"));
			verify(userRepository, times(1)).findOne(validUserId);
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}

	@Test
	public void shouldDeleteUser() throws Exception {
		final Long userId = 124L;
		when(userRepository.findOne(userId)).thenReturn(User.builder().build());

		testee.delete(userId);
		verify(userRepository, times(1)).delete(userId);
		verify(userRepository, times(1)).findOne(userId);
		verifyNoMoreInteractions(userRepository);
	}

	@Test(expected = NotFoundException.class)
	public void shouldThrowNotFoundExceptionForUnkownUserId() throws Exception {
		final Long userId = 124L;
		when(userRepository.findOne(userId)).thenReturn(null);

		try {
			testee.delete(userId);
		} catch (InvalidUserException e) {
			assertThat(e.getMessage(), is("id not found"));
			verify(userRepository, times(1)).findOne(userId);
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}
}
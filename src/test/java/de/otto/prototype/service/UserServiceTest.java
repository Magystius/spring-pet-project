package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
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

	private static final String validUserId = "someUserId";


	private static final Login validLogin =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final Login validLoginWithId =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final User validMinimumUser =
			User.builder().lastName("Mustermann").firstName("Max").age(30).login(validLogin).build();
	private static final User validMinimumUserWithId =
			User.builder().id(validUserId).lastName("Mustermann").firstName("Max").age(30).login(validLoginWithId).build();

	@Mock
	private UserRepository userRepository;

	private UserService testee;

	@Before
	public void setUp() throws Exception {
		LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
		validatorFactory.setProviderClass(HibernateValidator.class);
		validatorFactory.afterPropertiesSet();

		testee = new UserService(userRepository, validatorFactory);
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
		String userId = "someId";
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
		String userId = "someId";
		when(userRepository.findOne(userId)).thenReturn(null);

		final Optional<User> foundUser = testee.findOne(userId);

		assertThat(foundUser.isPresent(), is(false));
		verify(userRepository, times(1)).findOne(userId);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	public void shouldReturnCreatedUser() throws Exception {
		User persistedUser = validMinimumUser.toBuilder().id("someId").build();
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
		User invalidUserToCreate = validMinimumUser.toBuilder().login(validLogin.toBuilder().mail("max.mustermann@web.de").build()).build();

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
		final User updatedUser = validMinimumUserWithId.toBuilder().lastName("Neumann").build();
		when(userRepository.findOne(validUserId)).thenReturn(validMinimumUserWithId);
		when(userRepository.save(updatedUser)).thenReturn(updatedUser);

		final User persistedUser = testee.update(updatedUser, null);

		assertThat(persistedUser.getLastName(), is("Neumann"));
		assertThat(persistedUser.getId(), is(validUserId));
		verify(userRepository, times(1)).findOne(validUserId);
		verify(userRepository, times(1)).save(updatedUser);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	public void shouldReturnUpdatedUserIfETagsAreEqual() throws Exception {
		final User updatedUser = validMinimumUserWithId.toBuilder().lastName("Neumann").build();
		when(userRepository.findOne(validUserId)).thenReturn(validMinimumUserWithId);
		when(userRepository.save(updatedUser)).thenReturn(updatedUser);

		final User persistedUser = testee.update(updatedUser, validMinimumUserWithId.getETag());

		assertThat(persistedUser.getLastName(), is("Neumann"));
		assertThat(persistedUser.getId(), is(validUserId));
		verify(userRepository, times(1)).findOne(validUserId);
		verify(userRepository, times(1)).save(updatedUser);
		verifyNoMoreInteractions(userRepository);
	}

	@Test(expected = ConcurrentModificationException.class)
	public void shouldThrowConcurrentModificationExceptionIfETagsUnequal() {
		when(userRepository.findOne(validUserId)).thenReturn(validMinimumUserWithId);
		try {
			testee.update(validMinimumUserWithId, "someDifferentEtag");
		} catch (ConcurrentModificationException e) {
			assertThat(e.getMessage(), is("etags aren´t equal"));
			verify(userRepository, times(1)).findOne(validUserId);
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldThrowConstraintViolationExceptionIfInvalidExistingUser() {
		User invalidUserToUpdate = validMinimumUserWithId.toBuilder().firstName("a").build();
		when(userRepository.findOne(validUserId)).thenReturn(invalidUserToUpdate);

		try {
			testee.update(invalidUserToUpdate, null);
		} catch (ConstraintViolationException e) {
			String msgCode = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			throw e;
		}
	}

	@Test(expected = InvalidUserException.class)
	public void shouldThrowInvalidUserExceptionOnExistingUserWithWrongMail() {
		User invalidUserToUpdate = validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().mail("max.mustermann@web.de").build()).build();
		when(userRepository.findOne(validUserId)).thenReturn(invalidUserToUpdate);

		try {
			testee.update(invalidUserToUpdate, null);
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
			testee.update(validMinimumUser.toBuilder().id(validUserId).build(), null);
		} catch (InvalidUserException e) {
			assertThat(e.getMessage(), is("id not found"));
			verify(userRepository, times(1)).findOne(validUserId);
			verifyNoMoreInteractions(userRepository);
			throw e;
		}
	}

	@Test
	public void shouldDeleteUser() throws Exception {
		String userId = "someId";
		when(userRepository.findOne(userId)).thenReturn(User.builder().build());

		testee.delete(userId);
		verify(userRepository, times(1)).delete(userId);
		verify(userRepository, times(1)).findOne(userId);
		verifyNoMoreInteractions(userRepository);
	}

	@Test(expected = NotFoundException.class)
	public void shouldThrowNotFoundExceptionForUnkownUserId() throws Exception {
		String userId = "someId";
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
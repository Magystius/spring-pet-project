package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class UserServiceTest {

	private static final String VALID_USER_ID = "someUserId";
	private static final Login VALID_LOGIN =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final Login VALID_LOGIN_WITH_ID =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final User VALID_MINIMUM_USER =
			User.builder().lastName("Mustermann").firstName("Max").age(30).login(VALID_LOGIN).build();
	private static final User VALID_MINIMUM_USER_WITH_ID =
			User.builder().id(VALID_USER_ID).lastName("Mustermann").firstName("Max").age(30).login(VALID_LOGIN_WITH_ID).build();

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService testee;

	@BeforeEach
	void setUp() throws Exception {
		initMocks(this);

		LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
		validatorFactory.setProviderClass(HibernateValidator.class);
		validatorFactory.afterPropertiesSet();

		testee = new UserService(userRepository, validatorFactory);
	}

	@Nested
	@DisplayName("when one or all users are requested it")
	class getUsers {
		@Test
		@DisplayName("should return an empty list if not users are found")
		void shouldReturnEmptyListIfNoUserIsFound() throws Exception {
			when(userRepository.streamAll()).thenReturn(Stream.of());

			final Stream<User> returnedList = testee.findAll();

			assertThat(returnedList.collect(toList()).size(), is(0));
		}

		@Test
		@DisplayName("should return a stream of all users found")
		void shouldReturnListOfUsersFound() throws Exception {
			final User userToReturn = User.builder().lastName("Mustermann").build();
			when(userRepository.streamAll()).thenReturn(Stream.of(userToReturn));

			final List<User> listOfReturnedUser = testee.findAll().collect(toList());

			final Supplier<Stream<User>> sup = listOfReturnedUser::stream;
			assertAll("stream of user",
					() -> assertThat(sup.get().collect(toList()).size(), is(1)),
					() -> assertThat(sup.get().collect(toList()).get(0), is(userToReturn)));
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should return an optional of found user for an id")
		void shouldReturnAUserIfFound() throws Exception {
			String userId = "someId";
			String userLastName = "Mustermann";
			final User userToReturn = User.builder().id(userId).lastName(userLastName).build();
			when(userRepository.findOne(userId)).thenReturn(userToReturn);

			final User foundUser = testee.findOne(userId).orElse(null);

			assert foundUser != null;
			assertAll("user",
					() -> assertThat(foundUser.getId(), is(userId)),
					() -> assertThat(foundUser.getLastName(), is(userLastName)));
			verify(userRepository, times(1)).findOne(userId);
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should an empty optional if no user found for id")
		void shouldReturnNoUserIfNotFound() throws Exception {
			String userId = "someId";
			when(userRepository.findOne(userId)).thenReturn(null);

			final Optional<User> foundUser = testee.findOne(userId);

			assertThat(foundUser.isPresent(), is(false));
			verify(userRepository, times(1)).findOne(userId);
			verifyNoMoreInteractions(userRepository);
		}
	}

	@Nested
	@DisplayName("when a new user is given to be persisted")
	class createUser {
		@Test
		@DisplayName("should persist and return the new user")
		void shouldReturnCreatedUser() throws Exception {
			User persistedUser = VALID_MINIMUM_USER.toBuilder().id("someId").build();
			when(userRepository.save(VALID_MINIMUM_USER)).thenReturn(persistedUser);
			when(userRepository.streamAll()).thenReturn(Stream.of());

			final User returnedUser = testee.create(VALID_MINIMUM_USER);

			assertThat(returnedUser, is(persistedUser));
			verify(userRepository, times(1)).save(VALID_MINIMUM_USER);
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw a constraint violation if an invalid user is about to persisted")
		void shouldThrowConstraintViolationExceptionIfInvalidNewUser() {
			ConstraintViolationException exception =
					assertThrows(ConstraintViolationException.class, () -> testee.create(VALID_MINIMUM_USER.toBuilder().firstName("a").build()));
			String msgCode = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an invalid user exception if the user has a wrong mail")
		void shouldThrowInvalidUserExceptionOnNewUserWithWrongMail() {
			User invalidUserToCreate = VALID_MINIMUM_USER.toBuilder().login(VALID_LOGIN.toBuilder().mail("max.mustermann@web.de").build()).build();
			InvalidUserException exception = assertThrows(InvalidUserException.class, () -> testee.create(invalidUserToCreate));
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(invalidUserToCreate)),
					() -> assertThat(exception.getErrorMsg(), is("only mails by otto allowed")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an invalid user exception if the user with same data already exists ")
		void shouldThrowInvalidUserExceptionOnNewUserIfUserAlreadyExists() {
			when(userRepository.streamAll()).thenReturn(Stream.of(VALID_MINIMUM_USER));
			InvalidUserException exception = assertThrows(InvalidUserException.class, () -> testee.create(VALID_MINIMUM_USER));
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(VALID_MINIMUM_USER)),
					() -> assertThat(exception.getErrorMsg(), is("this user does already exist")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}
	}

	@Nested
	@DisplayName("when a user is about be to updated")
	class updateUser {
		@Test
		@DisplayName("should update the user and return it")
		void shouldReturnUpdatedUser() throws Exception {
			final User updatedUser = VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Neumann").build();
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(VALID_MINIMUM_USER_WITH_ID);
			when(userRepository.save(updatedUser)).thenReturn(updatedUser);
			when(userRepository.streamAll()).thenReturn(Stream.of(VALID_MINIMUM_USER_WITH_ID.toBuilder().firstName("Heinz").build()));

			final User persistedUser = testee.update(updatedUser, null);

			assertAll("user",
					() -> assertThat(persistedUser.getLastName(), is("Neumann")),
					() -> assertThat(persistedUser.getId(), is(VALID_USER_ID)));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verify(userRepository, times(1)).save(updatedUser);
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should update a user and return it, if the given etag and the users one are equal")
		void shouldReturnUpdatedUserIfETagsAreEqual() throws Exception {
			final User updatedUser = VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Neumann").build();
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(VALID_MINIMUM_USER_WITH_ID);
			when(userRepository.save(updatedUser)).thenReturn(updatedUser);
			when(userRepository.streamAll()).thenReturn(Stream.of(VALID_MINIMUM_USER_WITH_ID.toBuilder().firstName("Heinz").build()));

			final User persistedUser = testee.update(updatedUser, VALID_MINIMUM_USER_WITH_ID.getETag());

			assertAll("user",
					() -> assertThat(persistedUser.getLastName(), is("Neumann")),
					() -> assertThat(persistedUser.getId(), is(VALID_USER_ID)));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verify(userRepository, times(1)).save(updatedUser);
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an concurrent modification exception if etags aren´t equal")
		void shouldThrowConcurrentModificationExceptionIfETagsUnequal() {
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(VALID_MINIMUM_USER_WITH_ID);
			ConcurrentModificationException exception =
					assertThrows(ConcurrentModificationException.class, () -> testee.update(VALID_MINIMUM_USER_WITH_ID, "someDifferentEtag"));
			assertThat(exception.getMessage(), is("etags aren´t equal"));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an constraint violation exception if updated user is invalid")
		void shouldThrowConstraintViolationExceptionIfInvalidExistingUser() {
			User invalidUserToUpdate = VALID_MINIMUM_USER_WITH_ID.toBuilder().firstName("a").build();
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(invalidUserToUpdate);
			ConstraintViolationException exception =
					assertThrows(ConstraintViolationException.class, () -> testee.update(invalidUserToUpdate, null));
			String msgCode = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an invalid user exception if updated user has invalid mail")
		void shouldThrowInvalidUserExceptionOnExistingUserWithWrongMail() {
			User invalidUserToUpdate = VALID_MINIMUM_USER_WITH_ID.toBuilder().login(VALID_LOGIN_WITH_ID.toBuilder().mail("max.mustermann@web.de").build()).build();
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(invalidUserToUpdate);
			InvalidUserException exception =
					assertThrows(InvalidUserException.class, () -> testee.update(invalidUserToUpdate, null));
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(invalidUserToUpdate)),
					() -> assertThat(exception.getErrorMsg(), is("only mails by otto allowed")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw an invalid user exception if user with same data already exists")
		void shouldThrowInvalidUserExceptionOnExistingUserIfUserAlreadyExists() {
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(VALID_MINIMUM_USER_WITH_ID);
			when(userRepository.streamAll()).thenReturn(Stream.of(VALID_MINIMUM_USER_WITH_ID));
			InvalidUserException exception =
					assertThrows(InvalidUserException.class, () -> testee.update(VALID_MINIMUM_USER_WITH_ID, null));
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(VALID_MINIMUM_USER_WITH_ID)),
					() -> assertThat(exception.getErrorMsg(), is("this user does already exist")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verify(userRepository, times(1)).streamAll();
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should return a not found exception if no user for given id is found")
		void shouldReturnNotFoundExceptionIfIdUnknown() throws Exception {
			when(userRepository.findOne(VALID_USER_ID)).thenReturn(null);
			NotFoundException exception =
					assertThrows(NotFoundException.class, () -> testee.update(VALID_MINIMUM_USER.toBuilder().id(VALID_USER_ID).build(), null));
			assertThat(exception.getMessage(), is("user not found"));
			verify(userRepository, times(1)).findOne(VALID_USER_ID);
			verifyNoMoreInteractions(userRepository);
		}
	}

	@Nested
	@DisplayName("when a user id is given to delete a user")
	class deleteUser {
		@Test
		@DisplayName("should delete the user")
		void shouldDeleteUser() throws Exception {
			String userId = "someId";
			when(userRepository.findOne(userId)).thenReturn(User.builder().build());

			testee.delete(userId);
			verify(userRepository, times(1)).delete(userId);
			verify(userRepository, times(1)).findOne(userId);
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		@DisplayName("should throw a not found exception if no user for given is found")
		void shouldThrowNotFoundExceptionForUnkownUserId() throws Exception {
			String userId = "someId";
			when(userRepository.findOne(userId)).thenReturn(null);
			NotFoundException exception = assertThrows(NotFoundException.class, () -> testee.delete(userId));
			assertThat(exception.getMessage(), is("user id not found"));
			verify(userRepository, times(1)).findOne(userId);
			verifyNoMoreInteractions(userRepository);
		}
	}
}
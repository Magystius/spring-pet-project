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
import org.mockito.Mock;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

class UserServiceTest {

	private static final String VALID_USER_ID = "someUserId";
	private static final Login VALID_LOGIN =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final User VALID_MINIMUM_USER =
			User.builder().lastName("Mustermann").firstName("Max").age(30).login(VALID_LOGIN).build();
	private static final User VALID_MINIMUM_USER_WITH_ID =
			VALID_MINIMUM_USER.toBuilder().id(VALID_USER_ID).build();

	@Mock
	private UserRepository userRepository;

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
		@DisplayName("should return an empty list if no users are found")
		void shouldReturnEmptyListIfNoUserIsFound() throws Exception {
			given(userRepository.findAll()).willReturn(Flux.empty());

			StepVerifier.create(testee.findAll())
					.verifyComplete();
		}

		@Test
		@DisplayName("should return a stream of all users found")
		void shouldReturnListOfUsersFound() throws Exception {
			given(userRepository.findAll()).willReturn(Flux.just(VALID_MINIMUM_USER_WITH_ID));

			StepVerifier.create(testee.findAll())
					.assertNext(user -> assertThat(user, is(VALID_MINIMUM_USER_WITH_ID)))
					.verifyComplete();
		}

		@Test
		@DisplayName("should return an optional of found user for an id")
		void shouldReturnAUserIfFound() throws Exception {
			final Mono<String> userIdToFind = Mono.just(VALID_USER_ID);
			final Mono<User> expectedUserToFind = Mono.just(VALID_MINIMUM_USER_WITH_ID);
			given(userRepository.findById(userIdToFind)).willReturn(expectedUserToFind);

			StepVerifier.create(testee.findOne(userIdToFind))
					.assertNext(foundUser -> assertThat(foundUser, is(VALID_MINIMUM_USER_WITH_ID)))
					.verifyComplete();
		}

		@Test
		@DisplayName("should an empty optional if no user found for id")
		void shouldReturnNoUserIfNotFound() throws Exception {
			final Mono<String> userIdNotToFind = Mono.just("someId");
			given(userRepository.findById(userIdNotToFind))
					.willReturn(Mono.empty());

			StepVerifier.create(testee.findOne(userIdNotToFind))
					.verifyComplete();
		}
	}

	@Nested
	@DisplayName("when a new user is given to be persisted")
	class createUser {
		@Test
		@DisplayName("should persist and return the new user")
		void shouldReturnCreatedUser() throws Exception {
			given(userRepository.save(VALID_MINIMUM_USER)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			given(userRepository.findAll()).willReturn(Flux.empty());

			StepVerifier.create(testee.create(Mono.just(VALID_MINIMUM_USER)))
					.assertNext(returnedUser -> assertThat(returnedUser, is(VALID_MINIMUM_USER_WITH_ID)))
					.verifyComplete();
		}

		//TODO: replace this with reactor test
		@Test
		@DisplayName("should throw an invalid user exception if the user has a wrong mail")
		void shouldThrowInvalidUserExceptionOnNewUserWithWrongMail() {
			User invalidUserToCreate = VALID_MINIMUM_USER.toBuilder().login(VALID_LOGIN.toBuilder().mail("max.mustermann@web.de").build()).build();
			InvalidUserException exception = assertThrows(InvalidUserException.class, () -> testee.create(Mono.just(invalidUserToCreate)).block());
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(invalidUserToCreate)),
					() -> assertThat(exception.getErrorMsg(), is("only mails by otto allowed")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(userRepository).should(never()).save(any(User.class));
		}

		//TODO: replace this with reactor test
		@Test
		@DisplayName("should throw an invalid user exception if the user with same data already exists ")
		void shouldThrowInvalidUserExceptionOnNewUserIfUserAlreadyExists() {
			given(userRepository.findAll()).willReturn(Flux.just(VALID_MINIMUM_USER_WITH_ID));
			InvalidUserException exception = assertThrows(InvalidUserException.class, () -> testee.create(Mono.just(VALID_MINIMUM_USER)).block());
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(VALID_MINIMUM_USER)),
					() -> assertThat(exception.getErrorMsg(), is("this user does already exist")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(userRepository).should(never()).save(any(User.class));
		}
	}

	@Nested
	@DisplayName("when a user is about be to updated")
	class updateUser {
		@Test
		@DisplayName("should update the user and return it")
		void shouldReturnUpdatedUser() throws Exception {
			final User updatedUser = VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Neumann").build();
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			given(userRepository.findAll()).willReturn(Flux.just(VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Heinz").build()));
			given(userRepository.save(updatedUser)).willReturn(Mono.just(updatedUser));

			StepVerifier.create(testee.update(Mono.just(updatedUser), Mono.empty()))
					.assertNext(user -> assertThat(user, is(updatedUser)))
					.verifyComplete();
		}

		@Test
		@DisplayName("should update a user and return it, if the given etag and the users one are equal")
		void shouldReturnUpdatedUserIfETagsAreEqual() throws Exception {
			final User updatedUser = VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Neumann").build();
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			given(userRepository.findAll()).willReturn(Flux.just(VALID_MINIMUM_USER_WITH_ID.toBuilder().lastName("Heinz").build()));
			given(userRepository.save(updatedUser)).willReturn(Mono.just(updatedUser));


			StepVerifier.create(testee.update(Mono.just(updatedUser), Mono.just(VALID_MINIMUM_USER_WITH_ID.getETag())))
					.assertNext(user -> assertThat(user, is(updatedUser)))
					.verifyComplete();
		}

		@Test
		@DisplayName("should throw an constraint violation exception if updated user is invalid")
		void shouldThrowConstraintViolationExceptionIfInvalidExistingUser() {
			User invalidUserToUpdate = VALID_MINIMUM_USER_WITH_ID.toBuilder().firstName("a").build();
			ConstraintViolationException exception =
					assertThrows(ConstraintViolationException.class, () -> testee.update(Mono.just(invalidUserToUpdate), Mono.empty()).block());
			String msgCode = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			then(userRepository).should(never()).save(any(User.class));
		}

		@Test
		@DisplayName("should return a not found exception if no user for given id is found")
		void shouldReturnNotFoundExceptionIfIdUnknown() throws Exception {
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.empty());
			NotFoundException exception =
					assertThrows(NotFoundException.class, () -> testee.update(Mono.just(VALID_MINIMUM_USER_WITH_ID), Mono.empty()).block());
			assertThat(exception.getMessage(), is("user not found"));
			then(userRepository).should(never()).save(any(User.class));
		}

		@Test
		@DisplayName("should throw an concurrent modification exception if etags aren´t equal")
		void shouldThrowConcurrentModificationExceptionIfETagsUnequal() {
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			ConcurrentModificationException exception =
					assertThrows(ConcurrentModificationException.class, () -> testee.update(Mono.just(VALID_MINIMUM_USER_WITH_ID), Mono.just("someDifferentEtag")).block());
			assertThat(exception.getMessage(), is("etags aren´t equal"));
			then(userRepository).should(never()).save(any(User.class));
		}

		@Test
		@DisplayName("should throw an invalid user exception if updated user has invalid mail")
		void shouldThrowInvalidUserExceptionOnExistingUserWithWrongMail() {
			User invalidUserToUpdate = VALID_MINIMUM_USER_WITH_ID.toBuilder().login(VALID_LOGIN.toBuilder().mail("max.mustermann@web.de").build()).build();
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			InvalidUserException exception =
					assertThrows(InvalidUserException.class, () -> testee.update(Mono.just(invalidUserToUpdate), Mono.empty()).block());
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(invalidUserToUpdate)),
					() -> assertThat(exception.getErrorMsg(), is("only mails by otto allowed")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(userRepository).should(never()).save(any(User.class));
		}

		@Test
		@DisplayName("should throw an invalid user exception if user with same data already exists")
		void shouldThrowInvalidUserExceptionOnExistingUserIfUserAlreadyExists() {
			given(userRepository.findById(VALID_USER_ID)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			given(userRepository.findAll()).willReturn(Flux.just(VALID_MINIMUM_USER_WITH_ID));
			InvalidUserException exception =
					assertThrows(InvalidUserException.class, () -> testee.update(Mono.just(VALID_MINIMUM_USER_WITH_ID), Mono.empty()).block());
			assertAll("exception content",
					() -> assertThat(exception.getUser(), is(VALID_MINIMUM_USER_WITH_ID)),
					() -> assertThat(exception.getErrorMsg(), is("this user does already exist")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(userRepository).should(never()).save(any(User.class));
		}
	}

	@Nested
	@DisplayName("when a user id is given to delete a user")
	class deleteUser {
		@Test
		@DisplayName("should delete the user")
		void shouldDeleteUser() throws Exception {
			final Mono<String> userIdToDelete = Mono.just(VALID_USER_ID);
			given(userRepository.findById(userIdToDelete)).willReturn(Mono.just(VALID_MINIMUM_USER_WITH_ID));
			given(userRepository.delete(VALID_MINIMUM_USER_WITH_ID)).willReturn(Mono.empty());

			StepVerifier.create(testee.delete(userIdToDelete))
					.verifyComplete();

			then(userRepository).should(inOrder(userRepository)).findById(userIdToDelete);
			then(userRepository).should(inOrder(userRepository)).delete(VALID_MINIMUM_USER_WITH_ID);
		}

		@Test
		@DisplayName("should throw a not found exception if no user for given is found")
		void shouldThrowNotFoundExceptionForUnkownUserId() throws Exception {
			final Mono<String> invalidUserIdToDelete = Mono.just(VALID_USER_ID);
			given(userRepository.findById(invalidUserIdToDelete)).willReturn(Mono.empty());

			NotFoundException exception = assertThrows(NotFoundException.class, () -> testee.delete(invalidUserIdToDelete).block());
			assertThat(exception.getMessage(), is("user not found"));
			then(userRepository).should(never()).delete(any(User.class));
		}
	}
}
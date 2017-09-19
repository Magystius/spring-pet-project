package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class PasswordServiceTest {

    @Mock
    private UserService userService;

    private PasswordService testee;

    @BeforeEach
    void setUp() throws Exception {
        initMocks(this);
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setProviderClass(HibernateValidator.class);
        validatorFactory.afterPropertiesSet();

        testee = new PasswordService(userService, validatorFactory);
    }

    //TODO: make this test better
    @Nested
    @DisplayName("when a new password for a user id is send")
    class updatePassword {
        @Test
        @DisplayName("should update the password and return the updated user")
        void shouldReturnUpdatedUser() throws Exception {
            final String userId = "someId";
            final String password = "somePassword";
            final Mono<String> userIdMono = Mono.just(userId);
            final Mono<String> passwordMono = Mono.just(password);
            final Mono<User> userToUpdate = Mono.just(User.builder().id(userId).lastName("Mustermann").login(Login.builder().build()).build());
            final Mono<User> updatedUser = Mono.just(User.builder().id(userId).lastName("Mustermann").login(Login.builder().password(password).build()).build());
            given(userService.findOne(userIdMono)).willReturn(userToUpdate);
            given(userService.update(argThat(userMono -> Objects.equals(userMono.block(), updatedUser.block())), any())).willReturn(updatedUser);

            StepVerifier.create(testee.update(userIdMono, passwordMono))
                    .assertNext(persistedUser -> assertThat(persistedUser, is(updatedUser.block())))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw a not found exception if id for user is unknown")
        void shouldReturnNotFoundExceptionIfUnknownId() throws Exception {
            final Mono<String> nonExistingUserId = Mono.just("unknownId");
            given(userService.findOne(nonExistingUserId)).willReturn(Mono.empty());

            testee.update(nonExistingUserId, Mono.empty());

            ArgumentCaptor<Mono> toBeUpdatedUser = ArgumentCaptor.forClass(Mono.class);
            verify(userService).update(toBeUpdatedUser.capture(), any());
            final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> toBeUpdatedUser.getValue().block());
            assertThat(notFoundException.getMessage(), is("user not found"));
        }
    }

    @Nested
    @DisplayName("when a string is tested if it´s a secure password")
    class checkPassword {
        @Test
        @DisplayName("should return false for a unsecure password")
        void shouldReturnFalseForInsecurePassword() {
            final Mono<String> unsecPW = Mono.just("unsec");
            StepVerifier.create(testee.checkPassword(unsecPW))
                    .assertNext(result -> assertThat(result, is(false)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return true for a secure password")
        void shouldReturnTrueForSecurePassword() {
            final Mono<String> securePW = Mono.just("securePassword");
            StepVerifier.create(testee.checkPassword(securePW))
                    .assertNext(result -> assertThat(result, is(true)))
                    .verifyComplete();
        }
    }
}
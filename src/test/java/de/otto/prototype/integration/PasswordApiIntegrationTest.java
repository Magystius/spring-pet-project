package de.otto.prototype.integration;

import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.stream.Stream;

import static de.otto.prototype.controller.PasswordController.URL_RESET_PASSWORD;
import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

class PasswordApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private static Stream<Arguments> invalidRequestProvider() {
        return Stream.of(
                Arguments.of(POST, "012346789101112131415161", "unsec", "error.password", "updateUserPassword.password"),
                Arguments.of(POST, "0", "securePassword", "error.id.invalid", "updateUserPassword.id"),
                Arguments.of(POST, "", "securePassword", "error.id.invalid", "updateUserPassword.id"));
    }

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll().block();
        messageSource = initMessageSource();
        this.base = new URL("http://localhost:" + port + URL_RESET_PASSWORD);
    }

    @ParameterizedTest(name = "{0}: userId={1}, password={2}, error={4}")
    @MethodSource("invalidRequestProvider")
    @DisplayName("should return a bad request if")
    void shouldReturnBadRequestForInvalidParameterGiven(HttpMethod httpMethod, String userId, String password, String error, String attribute) throws Exception {

        final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=" + userId,
                httpMethod,
                new HttpEntity<>(password,
                        prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
                String.class);

        String errorMessage = messageSource.getMessage(error, null, LOCALE);
        ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute(attribute).errorMessage(errorMessage).build();
        ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
        assertThat(response.getStatusCode(), is(BAD_REQUEST));
        assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
    }

    @Nested
    @DisplayName("when the password endpoint is accessed")
    class happyPath {
        @Test
        @DisplayName("should the location for updated user when password is updated")
        void shouldReturnUpdatedUserOnPost() throws Exception {
            final Login login = Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
            final User persistedUser = userRepository.save(User.builder().lastName("Mustermann").firstName("Max").age(30).login(login).build()).block();
            final String newPassword = "anotherPassword";

            final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=" + persistedUser.getId(),
                    POST,
                    new HttpEntity<>(newPassword,
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
                    String.class);

            assertAll("response",
                    () -> assertThat(response.getStatusCode(), is(NO_CONTENT)),
                    () -> assertThat(response.getHeaders().get("Location").get(0), containsString(URL_USER + "/" + persistedUser.getId())));
        }

        @Test
        @DisplayName("should return true if a secure password is checked")
        void shouldReturnTrueForSecurePasswordOnPost() throws Exception {
            final ResponseEntity<String> response = template.exchange("http://localhost:" + port + "/checkpassword",
                    POST,
                    new HttpEntity<>("securePassword", prepareMediaTypeHeaders(TEXT_PLAIN_VALUE, TEXT_PLAIN_VALUE)),
                    String.class);

            assertAll("response",
                    () -> assertThat(response.getStatusCode(), is(OK)),
                    () -> assertThat(response.getBody(), is("true")));
        }
    }
}

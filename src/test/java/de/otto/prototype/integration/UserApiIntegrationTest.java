package de.otto.prototype.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.sha256;
import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class UserApiIntegrationTest extends BaseIntegrationTest {

    private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
    private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        messageSource = initMessageSource();
        this.base = new URL("http://localhost:" + port + URL_USER);
    }

    private void assertUserRepresentation(String responseBody, User expectedUser) {
        DocumentContext parsedResponse = JsonPath.parse(responseBody);
        assertAll("user representation",
                () -> assertThat(GSON.fromJson(parsedResponse.read("$.content").toString(), User.class), is(expectedUser)),
                () -> assertThat(parsedResponse.read("$._links.self.href"), containsString("/user/" + expectedUser.getId())),
                () -> assertThat(parsedResponse.read("$._links.start.href"), containsString("/user/" + expectedUser.getId())));
    }

    private void assertUserListRepresentation(User persistedUser, ResponseEntity<String> response) {
        DocumentContext parsedResponse = JsonPath.parse(response.getBody());
        assertAll("user list representation",
                () -> assertThat(parsedResponse.read("$._links.self.href"), containsString("/user")),
                () -> assertThat(parsedResponse.read("$._links.start.href"), containsString("/user/" + persistedUser.getId())),
                () -> assertThat(parsedResponse.read("$.total"), is(1)),
                () -> assertThat(parsedResponse.read("$.content[0]._links.self.href"), containsString("/user/" + persistedUser.getId())),
                () -> assertThat(GSON.fromJson(parsedResponse.read("$.content[0].content").toString(), User.class), is(persistedUser)));
    }

    @Nested
    @DisplayName("when the user endpoint is accessed")
    class happyPath {
        @Test
        @DisplayName("should return a list of previously saved users")
        void shouldReturnListOfUsersOnGetAll() throws Exception {
            User persistedUser = userRepository.save(user.login(login.build()).build());

            final String combinedETags = Stream.of(persistedUser)
                    .map(User::getETag)
                    .reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

            final ResponseEntity<String> response = template.exchange(base.toString(),
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            final User reducedRepresentationOfPersistedUser = persistedUser.toBuilder().login(null).age(0).build();
            assertUserListRepresentation(reducedRepresentationOfPersistedUser, response);
            final String eTagHeader = response.getHeaders().get(ETAG).get(0);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(eTag));
        }

        @Test
        @DisplayName("should return a previously saved user")
        void shouldReturnAUserOnGet() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build());

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            final String eTagHeader = response.getHeaders().get(ETAG).get(0);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(persistedUser.getETag()));
            assertUserRepresentation(response.getBody(), persistedUser);
        }

        @Test
        @DisplayName("should create a new user and return its locations & eTag header")
        void shouldCreateAUserOnPost() throws Exception {
            final ResponseEntity<String> response = template.exchange(base.toString(),
                    POST,
                    new HttpEntity<>(user.login(login.build()).build(),
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            final User createdUser = userRepository.findById(JsonPath.read(response.getBody(), "$.content.id")).get();
            assertThat(createdUser, is(notNullValue()));
            assertUserRepresentation(response.getBody(), createdUser);
            assertAll("response",
                    () -> assertThat(response.getStatusCode(), is(CREATED)),
                    () -> assertThat(response.getHeaders().get("Location").get(0).contains("/user/"), is(true)),
                    () -> assertThat(response.getHeaders().get(ETAG).get(0), is(createdUser.getETag())));
        }

        @Test
        @DisplayName("should update a previously saved user")
        void shouldUpdateAUserOnPut() throws Exception {
            final User userToUpdate = userRepository.save(user.login(login.build()).build());
            final String persistedId = userToUpdate.getId();
            final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
                    PUT,
                    new HttpEntity<>(updatedUser,
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            assertUserRepresentation(response.getBody(), updatedUser);
            assertThat(response.getHeaders().get(ETAG).get(0), is(updatedUser.getETag()));
        }

        @Test
        @DisplayName("should update a user when valid eTag is given")
        void shouldUpdateAUserWithETagOnPut() throws Exception {
            final User userToUpdate = userRepository.save(user.login(login.build()).build());
            final String persistedId = userToUpdate.getId();
            final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
                    PUT,
                    new HttpEntity<>(updatedUser,
                            prepareAuthAndMediaTypeAndIfMatchHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, userToUpdate.getETag())),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            assertUserRepresentation(response.getBody(), updatedUser);
            assertThat(response.getHeaders().get(ETAG).get(0), is(updatedUser.getETag()));
        }

        @Test
        @DisplayName("should delete a previously saved user")
        void shouldDeleteUserOnDelete() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build());

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
                    DELETE,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(NO_CONTENT));
        }
    }

    @Nested
    @DisplayName("when the user endpoint is accessed with an invalid id")
    class invalidId {
        @ParameterizedTest(name = "{0}")
        @EnumSource(value = HttpMethod.class, names = {"GET", "DELETE"})
        @DisplayName("should return a bad request on")
        void shouldReturnBadRequestIfInvalidId(HttpMethod httpMethod) throws Exception {
            final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
                    httpMethod,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(BAD_REQUEST));
            DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.errors[0].attribute"), endsWith("userId"));
        }

        @Test
        @DisplayName("should return a bad request if invalid id")
        void shouldReturnBadRequestIfInvalidIdOnPut() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final User updatedUser = persistedUser.toBuilder().firstName("newName").build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
                    PUT,
                    new HttpEntity<>(updatedUser,
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
            ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("update.userId").errorMessage(errorMessage).build();
            ValidationRepresentation<Group> returnedErrors = ValidationRepresentation.<Group>builder().error(errorEntry).build();
            assertThat(response.getStatusCode(), is(BAD_REQUEST));
            assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
        }
    }
}

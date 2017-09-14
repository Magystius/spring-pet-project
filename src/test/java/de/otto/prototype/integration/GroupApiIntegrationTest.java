package de.otto.prototype.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
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
import static de.otto.prototype.controller.GroupController.URL_GROUP;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class GroupApiIntegrationTest extends BaseIntegrationTest {

    private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
    private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);
    private static final Group.GroupBuilder group = Group.builder().name("someGroupName");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        messageSource = initMessageSource();
        this.base = new URL("http://localhost:" + port + URL_GROUP);
    }

    private void assertGroupRepresentation(String responseBody, Group expectedGroup) {
        DocumentContext parsedResponse = JsonPath.parse(responseBody);
        assertAll("group representation",
                () -> assertThat(GSON.fromJson(parsedResponse.read("$.content").toString(), Group.class), is(expectedGroup)),
                () -> assertThat(parsedResponse.read("$._links.self.href"), containsString("/group/" + expectedGroup.getId())),
                () -> assertThat(parsedResponse.read("$._links.start.href"), containsString("/group/" + expectedGroup.getId())));
    }

    private void assertGroupListRepresentation(Group persistedGroup, ResponseEntity<String> response) {
        DocumentContext parsedResponse = JsonPath.parse(response.getBody());
        assertAll("group list representation",
                () -> assertThat(parsedResponse.read("$._links.self.href"), containsString("/group")),
                () -> assertThat(parsedResponse.read("$._links.start.href"), containsString("/group/" + persistedGroup.getId())),
                () -> assertThat(parsedResponse.read("$.total"), is(1)),
                () -> assertThat(parsedResponse.read("$.content[0]._links.self.href"), containsString("/group/" + persistedGroup.getId())),
                () -> assertThat(GSON.fromJson(parsedResponse.read("$.content[0].content").toString(), Group.class), is(persistedGroup)));
    }

    @Nested
    @DisplayName("when the group endpoint is accessed")
    class happyPath {
        @Test
        @DisplayName("should return a list of previously saved groups")
        void shouldReturnListOfGroupsOnGetAll() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();

            final String combinedETags = Stream.of(persistedGroup)
                    .map(Group::getETag)
                    .reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

            final ResponseEntity<String> response = template.exchange(base.toString(),
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            assertGroupListRepresentation(persistedGroup, response);
            final String eTagHeader = response.getHeaders().get(ETAG).get(0);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(eTag));
        }

        @Test
        @DisplayName("should return a previously saved group")
        void shouldReturnAGroupOnGet() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedGroup.getId(),
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            final String eTagHeader = response.getHeaders().get(ETAG).get(0);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(persistedGroup.getETag()));
            assertGroupRepresentation(response.getBody(), persistedGroup);
        }

        @Test
        @DisplayName("should create a new group and return location & etag header")
        void shouldCreateAGroupOnPost() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final ResponseEntity<String> response = template.exchange(base.toString(),
                    POST,
                    new HttpEntity<>(group.clearUserIds().userId(persistedUser.getId()).build(),
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(CREATED));
            final Group createdGroup = groupRepository.findById((String) JsonPath.read(response.getBody(), "$.content.id")).block();
            assertThat(createdGroup, is(notNullValue()));
            assertGroupRepresentation(response.getBody(), createdGroup);
            assertAll("response headers",
                    () -> assertThat(response.getHeaders().get("Location").get(0).contains("/group/"), is(true)),
                    () -> assertThat(response.getHeaders().get(ETAG).get(0), is(createdGroup.getETag())));
        }

        @Test
        @DisplayName("should update a previously saved group")
        void shouldUpdateAGroupOnPut() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();
            final String persistedId = groupToUpdate.getId();
            final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
                    PUT,
                    new HttpEntity<>(updatedGroup,
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            assertGroupRepresentation(response.getBody(), updatedGroup);
            assertThat(response.getHeaders().get(ETAG).get(0), is(updatedGroup.getETag()));
        }

        @Test
        @DisplayName("should update a group when an eTag is given")
        void shouldUpdateAGroupWithETagOnPut() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();
            final String persistedId = groupToUpdate.getId();
            final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
                    PUT,
                    new HttpEntity<>(updatedGroup,
                            prepareAuthAndMediaTypeAndIfMatchHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, groupToUpdate.getETag())),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            assertGroupRepresentation(response.getBody(), updatedGroup);
            assertThat(response.getHeaders().get(ETAG).get(0), is(updatedGroup.getETag()));
        }

        @Test
        @DisplayName("should delete a previously saved group")
        void shouldDeleteGroupOnDelete() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedGroup.getId(),
                    DELETE,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(NO_CONTENT));
        }

    }

    @Nested
    @DisplayName("when the group endpoint is accessed with an invalid id")
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
            assertThat(parsedResponse.read("$.errors[0].attribute"), endsWith("groupId"));
        }

        @Test
        @DisplayName("should return a bad request if invalid id")
        void shouldReturnBadRequestIfInvalidIdOnPut() throws Exception {
            final User persistedUser = userRepository.save(user.login(login.build()).build()).block();
            final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build()).block();
            final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

            final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
                    PUT,
                    new HttpEntity<>(updatedGroup,
                            prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
            ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("update.groupId").errorMessage(errorMessage).build();
            ValidationRepresentation<Group> returnedErrors = ValidationRepresentation.<Group>builder().error(errorEntry).build();
            assertThat(response.getStatusCode(), is(BAD_REQUEST));
            assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
        }
    }
}

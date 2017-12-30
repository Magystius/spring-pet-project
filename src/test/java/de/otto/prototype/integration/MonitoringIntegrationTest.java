package de.otto.prototype.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.http.HttpStatus.*;

//TODO: reset metrics for each test?
class MonitoringIntegrationTest extends BaseIntegrationTest {

    private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
    private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);
    private static final Group.GroupBuilder group = Group.builder().name("someGroupName");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        groupRepository.deleteAll();
        this.base = new URL("http://localhost:" + port);
    }

    @ParameterizedTest(name = "url = {0}")
    @ValueSource(strings = {"", "/health", "/status", "/info", "/metrics", "/trace"})
    @DisplayName("should be able to find the monitoring endpoint ")
    void shouldAccessTheMonitoringEndpoint(String url) {
        final ResponseEntity<String> response = template
                .withBasicAuth("monitoring", "monitoring")
                .getForEntity(base.toString() + "/internal" + url, String.class);
        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    @DisplayName("should return metrics counter for service method executions")
    void shouldReturnMetricsWithCounterForServiceMethodsExecutions() {
        final User persistedUser = userRepository.save(user.login(login.build()).build());
        final ResponseEntity<String> invalidRequestResponse = performGetRequest("/user/" + persistedUser.getId());
        assertThat(invalidRequestResponse.getStatusCode(), is(OK));

        final ResponseEntity<String> response = performGetRequest("/internal/metrics/UserService.findAll");
        assertThat(response.getStatusCode(), is(OK));
        final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
        assertThat(parsedResponse.read("$.name"), is("UserService.findAll"));
        assertThat(parsedResponse.read("$.measurements[0].statistic"), is("Count"));
        assertThat(parsedResponse.read("$.measurements[0].value"), greaterThanOrEqualTo(1.0));
    }

    @Nested
    @DisplayName("when the metrics for http requests endpoint is accessed")
    class requestEndpoint {
        @Test
        @DisplayName("should return metrics with an ConstraintViolationException")
        void shouldReturnMetricsWithConstraintViolationException() {
            final ResponseEntity<String> invalidRequestResponse = performGetRequest("/user/invalidId");
            assertThat(invalidRequestResponse.getStatusCode(), is(BAD_REQUEST));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "ConstraintViolationException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("ConstraintViolationException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), isIn(List.of("GET", "PUT", "POST", "DELETE")));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/user/{userId}", "/group/{groupId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("400"));
        }

        @Test
        @DisplayName("should return metrics with an MethodArgumentNotValidException")
        void shouldReturnMetricsWithMethodArgumentNotValidException() {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final User invalidToBeUpdatedUser = persistedUser.toBuilder().age(10).build();
            final ResponseEntity<String> invalidRequestResponse = performPutRequest("/user/" + persistedUser.getId(), invalidToBeUpdatedUser, null);
            assertThat(invalidRequestResponse.getStatusCode(), is(BAD_REQUEST));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "MethodArgumentNotValidException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("MethodArgumentNotValidException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), isIn(List.of("PUT", "POST")));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/user/{userId}", "/group/{groupId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("400"));
        }

        @Test
        @DisplayName("should return metrics with an InvalidUserException")
        void shouldReturnMetricsWithInvalidUserException() {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final User invalidToBeUpdatedUser = persistedUser.toBuilder().login(login.mail("max.mustermann@invalid.de").build()).build();
            final ResponseEntity<String> invalidRequestResponse = performPutRequest("/user/" + persistedUser.getId(), invalidToBeUpdatedUser, null);
            assertThat(invalidRequestResponse.getStatusCode(), is(BAD_REQUEST));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "InvalidUserException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("InvalidUserException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), isIn(List.of("PUT", "POST")));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/user", "/user/{userId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("400"));
        }

        @Test
        @DisplayName("should return metrics with an InvalidGroupException")
        void shouldReturnMetricsWithInvalidGroupException() {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final Group persistedGroup = groupRepository.save(group.userId(persistedUser.getId()).build());
            final Group invalidGroupToBeUpdated = persistedGroup.toBuilder().vip(true).build();
            final ResponseEntity<String> invalidRequestResponse = performPutRequest("/group/" + persistedGroup.getId(), invalidGroupToBeUpdated, null);
            assertThat(invalidRequestResponse.getStatusCode(), is(BAD_REQUEST));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "InvalidGroupException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("InvalidGroupException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), isIn(List.of("PUT", "POST")));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/group", "/group/{groupId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("400"));
        }

        @Test
        @DisplayName("should return metrics with an NotFoundException")
        void shouldReturnMetricsWithNotFoundException() {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final User toBeUpdatedInvalidUser = persistedUser.toBuilder().id("5a2b10d3f5b11d882invalid").build();
            final ResponseEntity<String> invalidRequestResponse = performPutRequest("/user/5a2b10d3f5b11d882invalid", toBeUpdatedInvalidUser, null);
            assertThat(invalidRequestResponse.getStatusCode(), is(NOT_FOUND));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "NotFoundException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("NotFoundException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), is("PUT"));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/user/{userId}", "/group/{groupId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("404"));
        }

        @Test
        @DisplayName("should return metrics with an ConcurrentModificationException")
        void shouldReturnMetricsWithConcurrentModificationException() {
            final User persistedUser = userRepository.save(user.login(login.build()).build());
            final ResponseEntity<String> invalidRequestResponse = performPutRequest("/user/" + persistedUser.getId(), persistedUser, "invalidETag");
            assertThat(invalidRequestResponse.getStatusCode(), is(PRECONDITION_FAILED));

            final ResponseEntity<String> response = performGetRequest("/internal/metrics/http.server.requests");
            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            final int column = findMetricColumnByException(parsedResponse, "ConcurrentModificationException");
            assertThat(parsedResponse.read("$.availableTags[0].values[" + column + "]"), is("ConcurrentModificationException"));
            assertThat(parsedResponse.read("$.availableTags[1].values[" + column + "]"), is("PUT"));
            assertThat(parsedResponse.read("$.availableTags[2].values[" + column + "]"), isIn(List.of("/user/{userId}", "/group/{groupId}")));
            assertThat(parsedResponse.read("$.availableTags[3].values[" + column + "]"), is("412"));
        }

        private int findMetricColumnByException(final DocumentContext parsedResponse, final String exception) {
            final List<String> exceptions = parsedResponse.read("$.availableTags[0].values");
            return exceptions.indexOf(exception);
        }
    }

    @Nested
    @DisplayName("when the info endpoint is accessed")
    class infoEndpoint {
        @Test
        @DisplayName("should return a info representation with users info")
        void shouldReturnInfosForUsers() {
            userRepository.save(User.builder().build());
            userRepository.save(User.builder().vip(true).build());

            final ResponseEntity<String> response = performGetRequest("/internal/info");

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.user.total"), is(2));
            assertThat(parsedResponse.read("$.user.vip"), is(1));
        }

        @Test
        @DisplayName("should return a info representation with groups info")
        void shouldReturnInfosForGroups() {
            groupRepository.save(Group.builder().build());
            groupRepository.save(Group.builder().vip(true).build());

            final ResponseEntity<String> response = performGetRequest("/internal/info");

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.group.total"), is(2));
            assertThat(parsedResponse.read("$.group.vip"), is(1));
        }

        @Test
        @DisplayName("should return a info representation with application info")
        void shouldReturnInfosOfApplication() {
            final ResponseEntity<String> response = performGetRequest("/internal/info");

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.app.name"), is("Spring Pet Project"));
            assertThat(parsedResponse.read("$.app.description"), is("a simple spring pet project for testing purposes"));
            assertThat(parsedResponse.read("$.app.version"), is("1.0.0"));
            assertThat(parsedResponse.read("$.technical.encoding"), is("UTF-8"));
            assertThat(parsedResponse.read("$.technical.java.source"), is("9.0.0"));
            assertThat(parsedResponse.read("$.technical.java.target"), is("9.0.0"));
        }

        @Test
        @DisplayName("should return a info representation with git info")
        void shouldReturnInfosOfGit() {
            final ResponseEntity<String> response = performGetRequest("/internal/info");

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.git.commit.message.full"), is("test commit"));
            assertThat(parsedResponse.read("$.git.commit.id"), is("1234"));
            assertThat(parsedResponse.read("$.git.commit.time"), is(1511220753000L));
            assertThat(parsedResponse.read("$.git.branch"), is("master"));
        }
    }
}

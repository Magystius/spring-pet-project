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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.http.HttpStatus.OK;

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
    @ValueSource(strings = {"", "/health", "/info", "/metrics"})
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

      final ResponseEntity<String> response = performGetRequest("/internal/metrics/UserService.findAll"); //TODO: test all usages?
        assertThat(response.getStatusCode(), is(OK));
        final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
        assertThat(parsedResponse.read("$.name"), is("UserService.findAll"));
      assertThat(parsedResponse.read("$.measurements[0].statistic"), is("COUNT"));
        assertThat(parsedResponse.read("$.measurements[0].value"), greaterThanOrEqualTo(1.0));
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
          assertThat(parsedResponse.read("$.git.commit.id.full"), is("1234"));
          assertThat(parsedResponse.read("$.git.commit.time"), is("2017-11-20T23:32:33Z"));
            assertThat(parsedResponse.read("$.git.branch"), is("master"));
        }
    }

  @Nested
  @DisplayName("when the extended health endpoint is accessed")
  class healthEndpoint {
    @Test
    @DisplayName("should return 'up' as health status")
    void shouldReturnInfosForUsers() {
      final ResponseEntity<String> response = performGetRequest("/internal/health");

      assertThat(response.getStatusCode(), is(OK));
      final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
      assertThat(parsedResponse.read("$.status"), is("UP"));
    }

    @Test
    @DisplayName("should return infos about disk usage")
    void shouldReturnInfosForGroups() {
      final ResponseEntity<String> response = performGetRequest("/internal/health");

      assertThat(response.getStatusCode(), is(OK));
      final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
      assertThat(parsedResponse.read("$.details.diskSpace.status"), is("UP"));
      assertThat(parsedResponse.read("$.details.diskSpace.details.total"), notNullValue());
      assertThat(parsedResponse.read("$.details.diskSpace.details.free"), notNullValue());
      assertThat(parsedResponse.read("$.details.diskSpace.details.threshold"), notNullValue());
    }

    @Test
    @DisplayName("should return a infos about the used mongo db")
    void shouldReturnInfosOfApplication() {
      final ResponseEntity<String> response = performGetRequest("/internal/health");

      assertThat(response.getStatusCode(), is(OK));
      final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
      assertThat(parsedResponse.read("$.details.mongo.status"), is("UP"));
      assertThat(parsedResponse.read("$.details.mongo.details.version"), is("3.2.2"));
    }
  }
}

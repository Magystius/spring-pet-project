package de.otto.prototype.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.model.Group;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class MonitoringIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;


    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        groupRepository.deleteAll();
        this.base = new URL("http://localhost:" + port + "/internal");
    }

    @ParameterizedTest(name = "url = {0}")
    @ValueSource(strings = {"", "/health", "/status", "/info", "/metrics", "/trace"})
    @DisplayName("should be able to find the monitoring endpoint ")
    void shouldAccessTheMonitoringEndpoint(String url) {
        final ResponseEntity<String> response = template
                .withBasicAuth("monitoring", "monitoring")
                .getForEntity(base.toString() + url, String.class);
        assertThat(response.getStatusCode(), is(OK));
    }

    @Nested
    @DisplayName("when the info endpoint is accessed")
    class infoEndpoint {
        @Test
        @DisplayName("should return a info representation with users info")
        void shouldReturnInfosForUsers() {
            userRepository.save(User.builder().build());
            userRepository.save(User.builder().vip(true).build());

            final ResponseEntity<String> response = template.exchange(base.toString() + "/info",
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

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

            final ResponseEntity<String> response = template.exchange(base.toString() + "/info",
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.group.total"), is(2));
            assertThat(parsedResponse.read("$.group.vip"), is(1));
        }

        @Test
        @DisplayName("should return a info representation with application info")
        void shouldReturnInfosOfApplication() {
            final ResponseEntity<String> response = template.exchange(base.toString() + "/info",
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

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
            final ResponseEntity<String> response = template.exchange(base.toString() + "/info",
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                    String.class);

            assertThat(response.getStatusCode(), is(OK));
            final DocumentContext parsedResponse = JsonPath.parse(response.getBody());
            assertThat(parsedResponse.read("$.git.commit.message.full"), is("test commit"));
            assertThat(parsedResponse.read("$.git.commit.id"), is("1234"));
            assertThat(parsedResponse.read("$.git.commit.time"), is(1511220753000L));
            assertThat(parsedResponse.read("$.git.branch"), is("master"));
        }
    }
}

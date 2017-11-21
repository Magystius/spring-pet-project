package de.otto.prototype.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

class SecurityIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port);
    }

    @Test
    @DisplayName("should allow access to index route")
    void shouldAllowAccessOnIndexRoute() {
        final ResponseEntity<String> response = template.exchange(base.toString(), GET, null, String.class);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Nested
    @DisplayName("when a restricted url is accessed")
    class forbidAccess {
        @Test
        @DisplayName("should forbid access if wrong user")
        void shouldForbidAccessIfUnknownAuthentification() {
            final ResponseEntity<String> response = template
                    .withBasicAuth("unknown", "unknown")
                    .getForEntity(base.toString(), String.class);

            assertThat(response.getStatusCode(), is(UNAUTHORIZED));
        }

        @ParameterizedTest(name = "url = {0}")
        @ValueSource(strings = {"/user", "/user/1234", "/group", "/group/1234", "/resetpassword",
                "/internal", "/internal/health", "/internal/status", "/internal/info", "/internal/metrics", "/internal/metrics/exampleMetric", "/internal/trace"})
        @DisplayName("should forbid access if no authentification is send for ")
        void shouldForbidAccessIfNotAuthenticated(String url) {
            final ResponseEntity<String> responseForBaseUrl = template.getForEntity(url, String.class);
            assertThat(responseForBaseUrl.getStatusCode(), is(UNAUTHORIZED));
        }

        @ParameterizedTest(name = "url = {0}")
        @ValueSource(strings = {"/user", "/user/1234", "/group", "/group/1234", "/resetpassword"})
        @DisplayName("should forbid access if monitoring user tries to access  ")
        void shouldForbidAccessIfMonitoringAccessBusinessEndpoints(String url) {
            final ResponseEntity<String> responseForBaseUrl = template
                    .withBasicAuth("monitoring", "monitoring")
                    .getForEntity(url, String.class);
            assertThat(responseForBaseUrl.getStatusCode(), is(FORBIDDEN));
        }

        @ParameterizedTest(name = "url = {0}")
        @ValueSource(strings = {"/internal", "/internal/health", "/internal/status", "/internal/info", "/internal/metrics", "/internal/metrics/exampleMetric", "/internal/trace"})
        @DisplayName("should forbid access if user user tries to access  ")
        void shouldForbidAccessIfUserAccessMonitoringEndpoints(String url) {
            final ResponseEntity<String> responseForBaseUrl = template
                    .withBasicAuth("user", "user")
                    .getForEntity(url, String.class);
            assertThat(responseForBaseUrl.getStatusCode(), is(FORBIDDEN));
        }
    }
}

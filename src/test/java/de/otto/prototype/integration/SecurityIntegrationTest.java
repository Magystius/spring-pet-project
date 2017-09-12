package de.otto.prototype.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class SecurityIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port);
    }

    @Test
    @DisplayName("should allow access to index route")
    void shouldAllowAccessOnIndexRoute() throws Exception {
        final ResponseEntity<String> response = template.exchange(base.toString(), GET, null, String.class);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Nested
    @DisplayName("when a restricted url is accessed")
    class restrictedUrls {
        @Test
        @DisplayName("should forbid access if wrong user")
        void shouldForbidAccessIfWrongUser() throws Exception {
            final ResponseEntity<String> response = template.exchange(base.toString(),
                    GET,
                    new HttpEntity<>(prepareAuthAndMediaTypeHeaders("unknown", "unknown", null, null)),
                    String.class);

            assertThat(response.getStatusCode(), is(UNAUTHORIZED));
        }

        @ParameterizedTest(name = "url = {0}")
        @ValueSource(strings = {"/user", "/user/1234", "/group", "/group/1234"})
        @DisplayName("should forbid access if no user for ")
        void shouldForbidAccessIfNoUserForUser(String url) throws Exception {
            final ResponseEntity<String> responseForBaseUrl = template.exchange(base.toString() + url, GET, null, String.class);
            assertThat(responseForBaseUrl.getStatusCode(), is(UNAUTHORIZED));
        }
    }
}

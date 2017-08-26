package de.otto.prototype.integration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class SecurityIntegrationTest extends AbstractIntegrationTest {

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port);
	}

	@Test
	public void shouldForbidAccessIfWrongUser() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString(),
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("unknown", "unknown", null, null)),
				String.class);

		assertThat(response.getStatusCode(), is(UNAUTHORIZED));
	}

	@Test
	public void shouldAllowAccessOnIndexRoute() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString(), GET, null, String.class);

		assertThat(response.getStatusCode(), is(OK));
	}

	@Test
	public void shouldForbidAccessIfNoUser() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "/user", GET, null, String.class);

		assertThat(response.getStatusCode(), is(UNAUTHORIZED));
	}

}

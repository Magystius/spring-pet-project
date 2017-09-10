package de.otto.prototype.integration;

import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static de.otto.prototype.controller.PasswordController.URL_RESET_PASSWORD;
import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class PasswordApiIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		messageSource = initMessageSource();
		this.base = new URL("http://localhost:" + port + URL_RESET_PASSWORD);
	}

	@After
	public void tearDown() throws Exception {
		userRepository.deleteAll();
	}

	@Test
	public void shouldReturnUpdatedUserOnPost() throws Exception {
		final Login login = Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
		final User persistedUser = userRepository.save(User.builder().lastName("Mustermann").firstName("Max").age(30).login(login).build());
		final String newPassword = "anotherPassword";

		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=" + persistedUser.getId(),
				POST,
				new HttpEntity<>(newPassword,
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(NO_CONTENT));
		assertThat(response.getHeaders().get("Location").get(0), containsString(URL_USER + "/" + persistedUser.getId()));
	}

	@Test
	public void shouldReturnBadRequestIfPasswordIsUnsecureOnPost() throws Exception {

		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=012346789101112131415161",
				POST,
				new HttpEntity<>("unsec",
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.password", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("updateUserPassword.password").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=0",
				POST,
				new HttpEntity<>("securePassword",
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfEmptyIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=",
				POST,
				new HttpEntity<>("securePassword",
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnTrueForSecurePasswordOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange("http://localhost:" + port + "/checkpassword",
				POST,
				new HttpEntity<>("securePassword", prepareMediaTypeHeaders(TEXT_PLAIN_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is("true"));
	}

}

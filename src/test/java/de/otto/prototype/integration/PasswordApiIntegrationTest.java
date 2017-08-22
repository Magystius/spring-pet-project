package de.otto.prototype.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.Locale;

import static de.otto.prototype.controller.PasswordController.URL_RESET_PASSWORD;
import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class PasswordApiIntegrationTest extends AbstractIntegrationTest {

	private static final Gson GSON = new GsonBuilder().serializeNulls().create();

	private static final Locale LOCALE = LocaleContextHolder.getLocale();

	private MessageSource messageSource;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
		messageBundle.setBasename("classpath:messages/messages");
		messageBundle.setDefaultEncoding("UTF-8");
		messageSource = messageBundle;
		this.base = new URL("http://localhost:" + port + URL_RESET_PASSWORD);
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
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(NO_CONTENT));
		assertThat(response.getHeaders().get("Location").get(0), containsString(URL_USER + "/" + persistedUser.getId()));
	}

	@Test
	public void shouldReturnBadRequestIfPasswordIsUnsecureOnPost() throws Exception {

		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=012346789101112131415161",
				POST,
				new HttpEntity<>("unsec",
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.password", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.password").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=0",
				POST,
				new HttpEntity<>("securePassword",
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfEmptyIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "?userId=",
				POST,
				new HttpEntity<>("securePassword",
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnTrueForSecurePasswordOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange("http://localhost:" + port + "/checkpassword",
				POST,
				new HttpEntity<>("securePassword", prepareSimpleHeaders(TEXT_PLAIN_VALUE, TEXT_PLAIN_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is("true"));
	}

}

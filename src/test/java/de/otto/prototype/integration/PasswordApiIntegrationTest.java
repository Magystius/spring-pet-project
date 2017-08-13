package de.otto.prototype.integration;


import com.google.gson.Gson;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.Locale;

import static de.otto.prototype.controller.PasswordController.URL_PASSWORD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PasswordApiIntegrationTest {

	private static final Gson GSON = new Gson();

	private static final Locale LOCALE = LocaleContextHolder.getLocale();

	@LocalServerPort
	private int port;

	private URL base;

	private MessageSource messageSource;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
		messageBundle.setBasename("classpath:messages/messages");
		messageBundle.setDefaultEncoding("UTF-8");
		messageSource = messageBundle;
		this.base = new URL("http://localhost:" + port + URL_PASSWORD);
		userRepository.deleteAll();
	}

	@Test
	public void shouldReturnUpdatedUserOnPost() throws Exception {
		final Login login = Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
		final User persistedUser = userRepository.save(User.builder().lastName("Mustermann").firstName("Max").age(30).login(login).build());
		final String newPassword = "anotherPassword";
		final User updatedUser = persistedUser.toBuilder().login(login.toBuilder().password(newPassword).build()).build();

		final ResponseEntity<String> response = template.postForEntity(base.toString() + "?userId=" + persistedUser.getId(), newPassword,
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is(GSON.toJson(updatedUser)));
	}

	@Test
	public void shouldReturnBadRequestIfPasswordIsUnsecureOnPost() throws Exception {
		final ResponseEntity<String> response = template.postForEntity(base.toString() + "?userId=1234", "unsec",
				String.class);

		String errorMessage = messageSource.getMessage("error.password", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.password").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.postForEntity(base.toString() + "?userId=0", "securePassword",
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfEmptyIdOnPost() throws Exception {
		final ResponseEntity<String> response = template.postForEntity(base.toString() + "?userId=", "securePassword",
				String.class);

		String errorMessage = messageSource.getMessage("error.id.empty", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("updateUserPassword.id").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}
}

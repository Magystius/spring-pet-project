package de.otto.prototype.integration;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.model.UserList;
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

import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class UserApiIntegrationTest extends AbstractIntegrationTest {

	private static final Gson GSON = new GsonBuilder().serializeNulls().create();

	private static final Locale LOCALE = LocaleContextHolder.getLocale();

	private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
	private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);

	private MessageSource messageSource;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		initMessageSource();
		this.base = new URL("http://localhost:" + port + URL_USER);
		userRepository.deleteAll();
	}

	private void initMessageSource() {
		ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
		messageBundle.setBasename("classpath:messages/messages");
		messageBundle.setDefaultEncoding("UTF-8");
		messageSource = messageBundle;
	}

	@Test
	public void shouldReturnListOfUsersOnGetAll() throws Exception {
		User persistedUser1 = userRepository.save(user.login(login.build()).build());
		User persistedUser2 = userRepository.save(user.login(login.build()).build());

		final UserList listOfUsers = UserList.builder().user(persistedUser1).user(persistedUser2).build();
		final ResponseEntity<String> response = template.exchange(base.toString(),
				GET,
				new HttpEntity<>(prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is(GSON.toJson(listOfUsers)));
	}

	@Test
	public void shouldReturnAUserOnGet() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
				GET,
				new HttpEntity<>(prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		DocumentContext parsedResponse = JsonPath.parse(response.getBody());
		assertThat(parsedResponse.read("$.content.id"), is(persistedUser.getId()));
		assertThat(parsedResponse.read("$.content.firstName"), is(persistedUser.getFirstName()));
		assertThat(parsedResponse.read("$.content.secondName"), is(persistedUser.getSecondName()));
		assertThat(parsedResponse.read("$.content.lastName"), is(persistedUser.getLastName()));
		assertThat(parsedResponse.read("$.content.age"), is(persistedUser.getAge()));
		assertThat(parsedResponse.read("$.content.vip"), is(persistedUser.isVip()));
		assertThat(parsedResponse.read("$.content.login.mail"), is(persistedUser.getLogin().getMail()));
		assertThat(parsedResponse.read("$.content.login.password"), is(persistedUser.getLogin().getPassword()));
		assertThat(parsedResponse.read("$.content.bio"), is(persistedUser.getBio()));
		assertThat(parsedResponse.read("$._links.self.href"), containsString("/user/" + persistedUser.getId()));
	}

	@Test
	public void shouldCreateAUserOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString(),
				POST,
				new HttpEntity<>(user.login(login.build()).build(),
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(CREATED));
		assertThat(response.getHeaders().get("Location").get(0).contains("/user/"), is(true));
	}

	@Test
	public void shouldUpdateAUserOnPut() throws Exception {
		final User userToUpdate = userRepository.save(user.login(login.build()).build());
		final String persistedId = userToUpdate.getId();
		final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
				PUT,
				new HttpEntity<>(updatedUser,
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is(GSON.toJson(updatedUser)));
	}

	@Test
	public void shouldDeleteUserOnDelete() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
				DELETE,
				new HttpEntity<>(prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(NO_CONTENT));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnGet() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				GET,
				new HttpEntity<>(prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("getOne.userId").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnPut() throws Exception {
		final User userToUpdate = userRepository.save(user.login(login.build()).build());
		final String persistedId = userToUpdate.getId();
		final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();
		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				PUT,
				new HttpEntity<>(updatedUser,
						prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("update.userId").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnDelete() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				DELETE,
				new HttpEntity<>(prepareCompleteHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		UserValidationEntryRepresentation errorEntry = UserValidationEntryRepresentation.builder().attribute("delete.userId").errorMessage(errorMessage).build();
		UserValidationRepresentation returnedErrors = UserValidationRepresentation.builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

}

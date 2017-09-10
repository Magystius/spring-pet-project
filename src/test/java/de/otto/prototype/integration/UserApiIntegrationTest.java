package de.otto.prototype.integration;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.sha256;
import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class UserApiIntegrationTest extends AbstractIntegrationTest {

	private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
	private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		userRepository.deleteAll();
		messageSource = initMessageSource();
		this.base = new URL("http://localhost:" + port + URL_USER);
	}

	@Test
	public void shouldReturnListOfUsersOnGetAll() throws Exception {
		User persistedUser1 = userRepository.save(user.login(login.build()).build());
		User persistedUser2 = userRepository.save(user.firstName("Heiko").login(login.build()).build());

		final String combinedETags = Stream.of(persistedUser1, persistedUser2)
				.map(User::getETag)
				.reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
		final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

		final ResponseEntity<String> response = template.exchange(base.toString(),
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		DocumentContext parsedResponse = JsonPath.parse(response.getBody());
		assertThat(parsedResponse.read("$._links.self.href"), containsString("/user"));
		assertThat(parsedResponse.read("$._links.start.href"), containsString("/user/" + persistedUser1.getId()));
		assertThat(parsedResponse.read("$.total"), is(2));
		assertThat(parsedResponse.read("$.content[0]._links.self.href"), containsString("/user/" + persistedUser1.getId()));
		assertThat(parsedResponse.read("$.content[0].content.id"), is(persistedUser1.getId()));
		assertThat(parsedResponse.read("$.content[0].content.firstName"), is("Max"));
		assertThat(parsedResponse.read("$.content[1]._links.self.href"), containsString("/user/" + persistedUser2.getId()));
		assertThat(parsedResponse.read("$.content[1].content.id"), is(persistedUser2.getId()));
		assertThat(parsedResponse.read("$.content[1].content.firstName"), is("Heiko"));
		final String eTagHeader = response.getHeaders().get(ETAG).get(0);
		assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(eTag));
	}

	@Test
	public void shouldReturnAUserOnGet() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		final String eTagHeader = response.getHeaders().get(ETAG).get(0);
		assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(persistedUser.getETag()));
		assertUserRepresentation(response.getBody(), persistedUser, true);
	}

	@Test
	public void shouldCreateAUserOnPost() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString(),
				POST,
				new HttpEntity<>(user.login(login.build()).build(),
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(CREATED));
		assertThat(response.getHeaders().get("Location").get(0).contains("/user/"), is(true));
		final User createdUser = userRepository.findOne((String) JsonPath.read(response.getBody(), "$.content.id"));
		assertThat(createdUser, is(notNullValue()));
		assertUserRepresentation(response.getBody(), createdUser, true);
		assertThat(response.getHeaders().get(ETAG).get(0), is(createdUser.getETag()));
	}

	@Test
	public void shouldUpdateAUserOnPut() throws Exception {
		final User userToUpdate = userRepository.save(user.login(login.build()).build());
		final String persistedId = userToUpdate.getId();
		final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
				PUT,
				new HttpEntity<>(updatedUser,
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertUserRepresentation(response.getBody(), updatedUser, true);
		assertThat(response.getHeaders().get(ETAG).get(0), is(updatedUser.getETag()));
	}

	@Test
	public void shouldUpdateAUserWithETagOnPut() throws Exception {
		final User userToUpdate = userRepository.save(user.login(login.build()).build());
		final String persistedId = userToUpdate.getId();
		final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
				PUT,
				new HttpEntity<>(updatedUser,
						prepareAuthAndMediaTypeAndIfMatchHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, userToUpdate.getETag())),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertUserRepresentation(response.getBody(), updatedUser, true);
		assertThat(response.getHeaders().get(ETAG).get(0), is(updatedUser.getETag()));
	}

	@Test
	public void shouldDeleteUserOnDelete() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(),
				DELETE,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(NO_CONTENT));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnGet() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("getOne.userId").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
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
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("update.userId").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnDelete() throws Exception {
		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				DELETE,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("delete.userId").errorMessage(errorMessage).build();
		ValidationRepresentation<User> returnedErrors = ValidationRepresentation.<User>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

}

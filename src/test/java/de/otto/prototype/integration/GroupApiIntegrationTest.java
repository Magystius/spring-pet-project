package de.otto.prototype.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
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
import static de.otto.prototype.controller.GroupController.URL_GROUP;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class GroupApiIntegrationTest extends BaseIntegrationTest {

	private static final Login.LoginBuilder login = Login.builder().mail("max.mustermann@otto.de").password("somePassword");
	private static final User.UserBuilder user = User.builder().lastName("Mustermann").firstName("Max").age(30);
	private static final Group.GroupBuilder group = Group.builder().name("someGroupName");

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GroupRepository groupRepository;

	@Before
	public void setUp() throws Exception {
		userRepository.deleteAll();
		groupRepository.deleteAll();
		messageSource = initMessageSource();
		this.base = new URL("http://localhost:" + port + URL_GROUP);
	}

	private void assertGroupRepresentation(String responseBody, Group expectedGroup) {
		DocumentContext parsedResponse = JsonPath.parse(responseBody);
		assertThat(parsedResponse.read("$.content.id"), is(expectedGroup.getId()));
		assertThat(parsedResponse.read("$.content.name"), is(expectedGroup.getName()));
		assertThat(parsedResponse.read("$.content.vip"), is(expectedGroup.isVip()));
		assertThat(parsedResponse.read("$.content.userIds[0]"), is(expectedGroup.getUserIds().get(0)));
		assertThat(parsedResponse.read("$._links.self.href"), containsString("/group/" + expectedGroup.getId()));
		assertThat(parsedResponse.read("$._links.start.href"), containsString("/group/" + expectedGroup.getId()));
	}

	@Test
	public void shouldReturnListOfGroupsOnGetAll() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());

		final String combinedETags = Stream.of(persistedGroup)
				.map(Group::getETag)
				.reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
		final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

		final ResponseEntity<String> response = template.exchange(base.toString(),
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		DocumentContext parsedResponse = JsonPath.parse(response.getBody());
		assertThat(parsedResponse.read("$._links.self.href"), containsString("/group"));
		assertThat(parsedResponse.read("$._links.start.href"), containsString("/group/" + persistedGroup.getId()));
		assertThat(parsedResponse.read("$.total"), is(1));
		assertThat(parsedResponse.read("$.content[0]._links.self.href"), containsString("/group/" + persistedGroup.getId()));
		assertThat(parsedResponse.read("$.content[0].content.id"), is(persistedGroup.getId()));
		assertThat(parsedResponse.read("$.content[0].content.name"), is("someGroupName"));
		assertThat(parsedResponse.read("$.content[0].content.userIds[0]"), is(persistedUser.getId()));
		final String eTagHeader = response.getHeaders().get(ETAG).get(0);
		assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(eTag));
	}

	@Test
	public void shouldReturnAGroupOnGet() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedGroup.getId(),
				GET,
				new HttpEntity<>(prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		final String eTagHeader = response.getHeaders().get(ETAG).get(0);
		assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(persistedGroup.getETag()));
		assertGroupRepresentation(response.getBody(), persistedGroup);
	}

	@Test
	public void shouldCreateAGroupOnPost() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final ResponseEntity<String> response = template.exchange(base.toString(),
				POST,
				new HttpEntity<>(group.clearUserIds().userId(persistedUser.getId()).build(),
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(CREATED));
		assertThat(response.getHeaders().get("Location").get(0).contains("/group/"), is(true));
		final Group createdGroup = groupRepository.findById(JsonPath.read(response.getBody(), "$.content.id")).get();
		assertThat(createdGroup, is(notNullValue()));
		assertGroupRepresentation(response.getBody(), createdGroup);
		assertThat(response.getHeaders().get(ETAG).get(0), is(createdGroup.getETag()));
	}

	@Test
	public void shouldUpdateAGroupOnPut() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());
		final String persistedId = groupToUpdate.getId();
		final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
				PUT,
				new HttpEntity<>(updatedGroup,
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertGroupRepresentation(response.getBody(), updatedGroup);
		assertThat(response.getHeaders().get(ETAG).get(0), is(updatedGroup.getETag()));
	}

	@Test
	public void shouldUpdateAGroupWithETagOnPut() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());
		final String persistedId = groupToUpdate.getId();
		final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId,
				PUT,
				new HttpEntity<>(updatedGroup,
						prepareAuthAndMediaTypeAndIfMatchHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, groupToUpdate.getETag())),
				String.class);

		assertThat(response.getStatusCode(), is(OK));
		assertGroupRepresentation(response.getBody(), updatedGroup);
		assertThat(response.getHeaders().get(ETAG).get(0), is(updatedGroup.getETag()));
	}

	@Test
	public void shouldDeleteGroupOnDelete() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group persistedGroup = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());

		final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedGroup.getId(),
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
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("getOne.groupId").errorMessage(errorMessage).build();
		ValidationRepresentation<Group> returnedErrors = ValidationRepresentation.<Group>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}

	@Test
	public void shouldReturnBadRequestIfInvalidIdOnPut() throws Exception {
		final User persistedUser = userRepository.save(user.login(login.build()).build());
		final Group groupToUpdate = groupRepository.save(group.clearUserIds().userId(persistedUser.getId()).build());
		final Group updatedGroup = groupToUpdate.toBuilder().name("newName").build();

		final ResponseEntity<String> response = template.exchange(base.toString() + "/0",
				PUT,
				new HttpEntity<>(updatedGroup,
						prepareAuthAndMediaTypeHeaders("admin", "admin", APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
				String.class);

		String errorMessage = messageSource.getMessage("error.id.invalid", null, LOCALE);
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("update.groupId").errorMessage(errorMessage).build();
		ValidationRepresentation<Group> returnedErrors = ValidationRepresentation.<Group>builder().error(errorEntry).build();
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
		ValidationEntryRepresentation errorEntry = ValidationEntryRepresentation.builder().attribute("delete.groupId").errorMessage(errorMessage).build();
		ValidationRepresentation<Group> returnedErrors = ValidationRepresentation.<Group>builder().error(errorEntry).build();
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), is(GSON.toJson(returnedErrors)));
	}
}

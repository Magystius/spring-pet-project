package de.otto.prototype.integration;


import com.google.gson.Gson;
import de.otto.prototype.model.User;
import de.otto.prototype.model.UserList;
import de.otto.prototype.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;

import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiIntegrationTest {

    private static final Gson GSON = new Gson();

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + URL_USER);
        userRepository.deleteAll();
    }

    @Test
    public void shouldReturnListOfUsersOnGetAll() throws Exception {
        final User persistedUser1 = User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();
        userRepository.save(persistedUser1);
        final User persistedUser2 = User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();
        userRepository.save(persistedUser2);

        final UserList listOfUsers = UserList.builder().user(persistedUser1).user(persistedUser2).build();
        final ResponseEntity<String> response = template.getForEntity(base.toString(),
                String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(GSON.toJson(listOfUsers)));
    }

    @Test
    public void shouldReturnAUserOnGet() throws Exception {
        final User persistedUser = userRepository.save(User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build());

        final ResponseEntity<String> response = template.getForEntity(base.toString() + "/" + persistedUser.getId(),
                String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(GSON.toJson(persistedUser)));
    }

    @Test
    public void shouldCreateAUserOnPost() throws Exception {
        final User userToPersist = User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();

        final ResponseEntity<String> response = template.postForEntity(base.toString(), userToPersist, String.class);

        assertThat(response.getStatusCode(), is(CREATED));
        assertThat(response.getHeaders().get("Location").get(0).contains("/user/"), is(true));
    }

    @Test
    public void shouldUpdateAUserOnPut() throws Exception {
        final User userToUpdate = User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();
        final Long persistedId = userRepository.save(userToUpdate).getId();
        final User updatedUser = userToUpdate.toBuilder().lastName("Neumann").id(persistedId).build();

        final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedId, PUT, new HttpEntity<>(updatedUser), String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(GSON.toJson(updatedUser)));
    }

    @Test
    public void shouldDeleteUserOnDelete() throws Exception {
        final User userToPersist = User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build();
        final User persistedUser = userRepository.save(userToPersist);

        final ResponseEntity<String> response = template.exchange(base.toString() + "/" + persistedUser.getId(), DELETE, null, String.class);

        assertThat(response.getStatusCode(), is(NO_CONTENT));
    }
}

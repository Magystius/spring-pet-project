package de.otto.prototype.integration;


import com.google.gson.Gson;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.Arrays;

import static de.otto.prototype.controller.UserController.URL_USER;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

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
    public void shouldReturnListOfUsersOnGet() throws Exception {

        User persistedUser1 = User.builder().lastName("Mustermann").firstName("Max").build();
        userRepository.save(persistedUser1);
        User persistedUser2 = User.builder().lastName("Lavendel").firstName("Lara").build();
        userRepository.save(persistedUser2);

        ResponseEntity<String> response = template.getForEntity(base.toString(),
                String.class);
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(GSON.toJson(Arrays.asList(persistedUser1, persistedUser2))));
    }

    @Test
    public void shouldCreateAUserOnPost() throws Exception {

        User userToPersist = User.builder().lastName("Mustermann").firstName("Max").build();

        ResponseEntity<String> response = template.postForEntity(base.toString(), userToPersist, String.class);
        assertThat(response.getStatusCode(), is(CREATED));
        assertThat(response.getHeaders().get("Location").get(0).contains("/user/"), is(true));
    }
}

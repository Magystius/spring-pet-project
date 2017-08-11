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

import static de.otto.prototype.controller.PasswordController.URL_PASSWORD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PasswordControllerIntegrationTest {

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
        this.base = new URL("http://localhost:" + port + URL_PASSWORD);
        userRepository.deleteAll();
    }

    @Test
    public void shouldReturnUpdatedUserOnGet() throws Exception {
        final User persistedUser = userRepository.save(User.builder().lastName("Mustermann").firstName("Max").password("somePassword").build());
        final String newPassword = "anotherPassword";
        final User updatedUser = persistedUser.toBuilder().password(newPassword).build();

        final ResponseEntity<String> response = template.getForEntity(base.toString() + "?id=" + persistedUser.getId() + "&password=" + newPassword,
                String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(GSON.toJson(updatedUser)));
    }
}

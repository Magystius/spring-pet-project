package de.otto.prototype.integration;


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
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

	@LocalServerPort
	private int port;

	private URL base;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/user");
		userRepository.deleteAll();
	}

	@Test
	public void shouldReturnListOfUsersOnGet() throws Exception {

		User persistedUser1 = User.builder().lastName("Mustermann").firstName("Max").build();
		userRepository.save(persistedUser1);
		User persistedUser2 = User.builder().lastName("Lavendel").firstName("Lara").build();
		userRepository.save(persistedUser2);

		String stringifiedUsers = Stream.of(persistedUser1, persistedUser2)
				.map(User::toString)
				.collect(joining("; "));

		ResponseEntity<String> response = template.getForEntity(base.toString(),
				String.class);
		assertThat(response.getStatusCode(), is(OK));
		assertThat(response.getBody(), is(stringifiedUsers));
	}

}

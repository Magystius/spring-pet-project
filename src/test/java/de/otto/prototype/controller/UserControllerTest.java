package de.otto.prototype.controller;

import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

	private MockMvc mvc;

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController testee;

	@Before
	public void init() {
		mvc = MockMvcBuilders
				.standaloneSetup(testee)
				.build();
	}

	@Test
	public void shouldReturnEmptyListIfNoUsersOnGet() throws Exception {
		when(userService.findAll()).thenReturn(Stream.of());

		mvc.perform(get("/user").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(is("")));

		verify(userService, times(1)).findAll();
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void shouldReturnListOfUsersOnGet() throws Exception {
		Supplier<Stream<User>> sup = () -> Stream.of(User.builder().lastName("Mustermann").build());
		String stringifiedUsers = sup.get()
				.map(User::toString)
				.collect(joining("; "));
		when(userService.findAll()).thenReturn(sup.get());

		mvc.perform(get("/user").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(is(stringifiedUsers)));

		verify(userService, times(1)).findAll();
		verifyNoMoreInteractions(userService);
	}
}
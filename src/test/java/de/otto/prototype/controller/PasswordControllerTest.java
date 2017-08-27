package de.otto.prototype.controller;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static de.otto.prototype.controller.PasswordController.URL_CHECK_PASSWORD;
import static de.otto.prototype.controller.PasswordController.URL_RESET_PASSWORD;
import static de.otto.prototype.controller.UserController.URL_USER;
import static java.lang.Boolean.valueOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordControllerTest {

	private MockMvc mvc;

	@Mock
	private PasswordService passwordService;

	@InjectMocks
	private PasswordController testee;

	@BeforeEach
	void setUp() {
		initMocks(this);
		mvc = MockMvcBuilders
				.standaloneSetup(testee)
				.build();
	}

	@Nested
	@DisplayName("when a new password for a user id is send")
	class resetPassword {
		@Test
		@DisplayName("should update the password and return a location header")
		void shouldUpdatePasswordOnPost() throws Exception {
			final String id = "someId";
			final String password = "somePassword";
			final User updatedUser = User.builder().id(id).firstName("Max").lastName("Mustermann").login(Login.builder().password(password).build()).build();
			when(passwordService.update(id, password)).thenReturn(updatedUser);

			final MvcResult result = mvc.perform(post(URL_RESET_PASSWORD + "?userId=" + id)
					.contentType(TEXT_PLAIN_VALUE)
					.accept(APPLICATION_JSON_VALUE)
					.content(password))
					.andDo(print())
					.andExpect(status().isNoContent())
					.andReturn();

			assertThat(result.getResponse().getHeader("Location"), containsString(URL_USER + "/" + id));

			verify(passwordService, times(1)).update(id, password);
			verifyNoMoreInteractions(passwordService);
		}

		@Test
		@DisplayName("should return a not found response if user id can´t be found")
		void shouldReturnNotFoundIfUnknownId() throws Exception {
			final String id = "someId";
			final String password = "somePassword";
			when(passwordService.update(id, password)).thenThrow(new NotFoundException("id not found"));

			mvc.perform(post(URL_RESET_PASSWORD + "?userId=" + id)
					.contentType(TEXT_PLAIN_VALUE)
					.accept(APPLICATION_JSON_VALUE)
					.content(password))
					.andDo(print())
					.andExpect(status().isNotFound());

			verify(passwordService, times(1)).update(id, password);
			verifyNoMoreInteractions(passwordService);
		}
	}

	@Nested
	@DisplayName("when a password is send to check if secure")
	class checkPassword {
		@ParameterizedTest(name = "-> result is {0}")
		@ValueSource(strings = {"true", "false"})
		@DisplayName("should return result of check")
		void shouldReturnTrueIfSecurePassword(String checkResult) throws Exception {
			final String password = "somePassword";
			when(passwordService.checkPassword(password)).thenReturn(valueOf(checkResult));

			mvc.perform(post(URL_CHECK_PASSWORD)
					.contentType(TEXT_PLAIN_VALUE)
					.accept(TEXT_PLAIN_VALUE)
					.content(password))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().string(checkResult));

			verify(passwordService, times(1)).checkPassword(password);
			verifyNoMoreInteractions(passwordService);
		}
	}
}
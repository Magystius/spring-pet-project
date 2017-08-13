package de.otto.prototype.controller;

import com.google.gson.Gson;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.service.PasswordService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static de.otto.prototype.controller.PasswordController.URL_PASSWORD;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class PasswordControllerTest {

    private static final Gson GSON = new Gson();

    private MockMvc mvc;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private PasswordController testee;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .standaloneSetup(testee)
                .build();
    }

    @Test
    public void shouldUpdatePasswordOnPost() throws Exception {
        final long id = 1234L;
        final String password = "somePassword";
        final User updatedUser = User.builder().id(id).firstName("Max").lastName("Mustermann").password(password).build();
        when(passwordService.update(id, password)).thenReturn(updatedUser);

        mvc.perform(post(URL_PASSWORD + "?userId=" + id)
                .contentType(TEXT_PLAIN_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(password))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(updatedUser))));

        verify(passwordService, times(1)).update(id, password);
        verifyNoMoreInteractions(passwordService);
    }

    @Test
    public void shouldReturnNotFoundIfUnknownId() throws Exception {
        final Long id = 1234L;
        final String password = "somePassword";
        when(passwordService.update(id, password)).thenThrow(new NotFoundException("id not found"));

        mvc.perform(post(URL_PASSWORD + "?userId=" + id)
                .contentType(TEXT_PLAIN_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(password))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(passwordService, times(1)).update(id, password);
        verifyNoMoreInteractions(passwordService);
    }

}
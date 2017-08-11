package de.otto.prototype.controller;

import com.google.gson.Gson;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.model.User;
import de.otto.prototype.model.UserList;
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

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    private static final Gson GSON = new Gson();

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

        mvc.perform(get(URL_USER).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(UserList.builder().build()))));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnListOfUsersOnGet() throws Exception {
        Supplier<Stream<User>> sup = () -> Stream.of(User.builder().lastName("Mustermann").build());
        UserList listOfUsers = UserList.builder().users(sup.get().collect(Collectors.toList())).build();
        when(userService.findAll()).thenReturn(sup.get());

        mvc.perform(get(URL_USER).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(listOfUsers))));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldCreateUserAndReturnItsLocationOnPost() throws Exception {
        User userToPersist = User.builder().firstName("Max").lastName("Mustermann").build();
        long persistedUserId = 1234L;
        when(userService.create(userToPersist)).thenReturn(userToPersist.toBuilder().id(persistedUserId).build());

        mvc.perform(post(URL_USER)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", URL_USER + "/" + persistedUserId));

        verify(userService, times(1)).create(userToPersist);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnBadRequestIfIdIsAlreadySetOnPost() throws Exception {
        User userToPersist = User.builder().firstName("Max").id(1234L).build();
        when(userService.create(userToPersist)).thenThrow(new InvalidUserException("id is already set"));

        mvc.perform(post(URL_USER)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).create(userToPersist);
        verifyNoMoreInteractions(userService);
    }
}
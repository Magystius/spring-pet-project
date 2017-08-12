package de.otto.prototype.controller;

import com.google.gson.Gson;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.otto.prototype.controller.UserController.URL_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    public void shouldReturnEmptyListIfNoUsersOnGetAll() throws Exception {
        when(userService.findAll()).thenReturn(Stream.of());

        mvc.perform(get(URL_USER).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(UserList.builder().build()))));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnListOfUsersOnGetAll() throws Exception {
        final Supplier<Stream<User>> sup = () -> Stream.of(User.builder().lastName("Mustermann").build());
        final UserList listOfUsers = UserList.builder().users(sup.get().collect(Collectors.toList())).build();
        when(userService.findAll()).thenReturn(sup.get());

        mvc.perform(get(URL_USER)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(listOfUsers))));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnAUserIfFoundOnGetOne() throws Exception {
        final Long userId = 1234L;
        final User userToFind = User.builder().id(userId).lastName("Mustermann").firstName("Max").build();

        when(userService.findOne(userId)).thenReturn(Optional.of(userToFind));

        mvc.perform(get(URL_USER + "/" + userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(is(GSON.toJson(userToFind))));

        verify(userService, times(1)).findOne(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturn404IfUserNotFoundOnGetOne() throws Exception {
        final Long userId = 1234L;

        when(userService.findOne(userId)).thenReturn(Optional.empty());

        mvc.perform(get(URL_USER + "/" + userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findOne(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldCreateUserAndReturnItsLocationOnPost() throws Exception {
        final User userToPersist = User.builder().firstName("Max").lastName("Mustermann").build();
        final Long persistedUserId = 1234L;
        when(userService.create(userToPersist)).thenReturn(userToPersist.toBuilder().id(persistedUserId).build());

        mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("location", URL_USER + "/" + persistedUserId));

        verify(userService, times(1)).create(userToPersist);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldUpdateUserOnPut() throws Exception {
        final Long userId = 1234L;
        final User updatedUser = User.builder().id(userId).firstName("Max").lastName("Mustermann").build();
        when(userService.update(updatedUser)).thenReturn(updatedUser);

        mvc.perform(put(URL_USER + "/" + userId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).update(updatedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNotFoundIfIDsDifferOnPut() throws Exception {
        final Long userId = 1234L;
        final User updatedUser = User.builder().id(userId).firstName("Max").lastName("Mustermann").build();

        mvc.perform(put(URL_USER + "/" + "differentId")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturNotFoundIfIdNotFoundOnPut() throws Exception {
        final Long userId = 1234L;
        final User updatedUser = User.builder().id(userId).firstName("Max").lastName("Mustermann").build();
        when(userService.update(updatedUser)).thenThrow(new NotFoundException("id not found"));

        mvc.perform(put(URL_USER + "/" + userId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).update(updatedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnBadRequestIfIdIsAlreadySetOnPost() throws Exception {
        final User userToPersist = User.builder().firstName("Max").id(1234L).build();
        when(userService.create(userToPersist)).thenThrow(new InvalidUserException("id is already set"));

        mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).create(userToPersist);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldDeleteUserOnDelete() throws Exception {
        final Long userIdToDelete = 1234L;

        mvc.perform(delete(URL_USER + "/" + userIdToDelete))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(userIdToDelete);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNotFoundIfUserIdNotFoundOnDelete() throws Exception {
        final Long userIdToDelete = 1234L;

        doThrow(new NotFoundException("id not found")).when(userService).delete(userIdToDelete);

        mvc.perform(delete(URL_USER + "/" + userIdToDelete))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).delete(userIdToDelete);
        verifyNoMoreInteractions(userService);
    }
}
package de.otto.prototype.service;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService testee;

    @Test
    public void shouldReturnEmptyListIfNoUserIsFound() throws Exception {
        when(userRepository.streamAll()).thenReturn(Stream.of());

        final Stream<User> returnedList = testee.findAll();

        assertThat(returnedList.collect(toList()).size(), is(0));
    }

    @Test
    public void shouldReturnListOfUsersFound() throws Exception {
        final User userToReturn = User.builder().lastName("Mustermann").build();
        when(userRepository.streamAll()).thenReturn(Stream.of(userToReturn));

        final List<User> listOfReturnedUser = testee.findAll().collect(toList());
        final Supplier<Stream<User>> sup = listOfReturnedUser::stream;
        assertThat(sup.get().collect(toList()).size(), is(1));
        assertThat(sup.get().collect(toList()).get(0), is(userToReturn));
        verify(userRepository, times(1)).streamAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldReturnAUserIfFound() throws Exception {
        Long userId = 1234L;
        String userLastName = "Mustermann";
        final User userToReturn = User.builder().id(userId).lastName(userLastName).build();
        when(userRepository.findOne(userId)).thenReturn(userToReturn);

        final User foundUser = testee.findOne(userId).orElse(null);
        assert foundUser != null;
        assertThat(foundUser.getId(), is(userId));
        assertThat(foundUser.getLastName(), is(userLastName));
        verify(userRepository, times(1)).findOne(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldReturnNoUserIfNotFound() throws Exception {
        Long userId = 1234L;
        when(userRepository.findOne(userId)).thenReturn(null);

        final Optional<User> foundUser = testee.findOne(userId);
        assertThat(foundUser.isPresent(), is(false));
        verify(userRepository, times(1)).findOne(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldReturnCreatedUser() throws Exception {
        final User userToPersist = User.builder().lastName("Mustermann").build();
        when(userRepository.save(userToPersist)).thenReturn(userToPersist.toBuilder().id(1234L).build());

        final User persistedUser = testee.create(userToPersist);
        assertThat(persistedUser.getLastName(), is("Mustermann"));
        assertThat(persistedUser.getId(), is(1234L));
        verify(userRepository, times(1)).save(userToPersist);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldReturnUpdatedUser() throws Exception {
        final long userId = 1234L;
        final User userToUpdate = User.builder().id(userId).lastName("Mustermann").build();
        final User updatedUser = User.builder().id(userId).lastName("Neumann").build();
        when(userRepository.findOne(userId)).thenReturn(userToUpdate);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        final User persistedUser = testee.update(updatedUser);
        assertThat(persistedUser.getLastName(), is("Neumann"));
        assertThat(persistedUser.getId(), is(userId));
        verify(userRepository, times(1)).findOne(userId);
        verify(userRepository, times(1)).save(updatedUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundExceptionIfIdIsNull() throws Exception {
        final User userToUpdate = User.builder().lastName("Mustermann").build();

        try {
            testee.update(userToUpdate);
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id not found"));
            verifyNoMoreInteractions(userRepository);
            throw e;
        }
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundExceptionIfIdUnknown() throws Exception {
        long userId = 1234L;
        final User userToUpdate = User.builder().id(userId).lastName("Mustermann").build();
        when(userRepository.findOne(userId)).thenReturn(null);

        try {
            testee.update(userToUpdate);
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id not found"));
            verify(userRepository, times(1)).findOne(userId);
            verifyNoMoreInteractions(userRepository);
            throw e;
        }
    }

    @Test(expected = InvalidUserException.class)
    public void shouldThrowInvalidUserExceptionIfUserIdIsSet() throws Exception {
        final User userToPersist = User.builder().id(1234L).build();

        try {
            testee.create(userToPersist);
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id is already set"));
            verify(userRepository, never()).save(any(User.class));
            throw e;
        }
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        final Long userId = 124L;
        when(userRepository.findOne(userId)).thenReturn(User.builder().build());

        testee.delete(userId);
        verify(userRepository, times(1)).delete(userId);
        verify(userRepository, times(1)).findOne(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionForUnkownUserId() throws Exception {
        final Long userId = 124L;
        when(userRepository.findOne(userId)).thenReturn(null);

        try {
            testee.delete(userId);
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id not found"));
            verify(userRepository, times(1)).findOne(userId);
            verifyNoMoreInteractions(userRepository);
            throw e;
        }
    }
}
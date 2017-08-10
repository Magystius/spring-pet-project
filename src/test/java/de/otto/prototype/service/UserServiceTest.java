package de.otto.prototype.service;

import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
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

		Stream<User> returnedList = testee.findAll();

		assertThat(returnedList.collect(toList()).size(), is(0));
	}

	@Test
	public void shouldReturnListOfUsersFound() throws Exception {
		User userToReturn = User.builder().lastName("Mustermann").build();
		when(userRepository.streamAll()).thenReturn(Stream.of(userToReturn));

		List<User> listOfReturnedUser = testee.findAll().collect(toList());
		Supplier<Stream<User>> sup = listOfReturnedUser::stream;
		assertThat(sup.get().collect(toList()).size(), is(1));
		assertThat(sup.get().collect(toList()).get(0), is(userToReturn));
		verify(userRepository, times(1)).streamAll();
		verifyNoMoreInteractions(userRepository);
	}
}
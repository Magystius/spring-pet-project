package de.otto.prototype.service;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class UserService {

	private UserRepository userRepository;

	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Stream<User> findAll() {
		return userRepository.streamAll();
	}

	public User create(User user) {
		if(user.getId() != null) {
			throw new InvalidUserException("id is already set");
		}
		return userRepository.save(user);
	}
}

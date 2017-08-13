package de.otto.prototype.service;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class UserService {

	private UserRepository userRepository;

	private Validator validator;

	@Autowired
	public UserService(UserRepository userRepository, Validator validator) {
		this.userRepository = userRepository;
		this.validator = validator;
	}

	public Stream<User> findAll() {
		return userRepository.streamAll();
	}

	public Optional<User> findOne(final Long userId) {
		return Optional.ofNullable(userRepository.findOne(userId));
	}

	public User create(final User user) {
		validateUser(user, User.New.class);
		return userRepository.save(user);
	}

	public User update(final User user) {
		if (userRepository.findOne(user.getId()) == null) {
			throw new NotFoundException("user not found");
		}
		validateUser(user, User.Existing.class);
		return userRepository.save(user);
	}

	public void delete(final Long userId) {
		if (userRepository.findOne(userId) == null) {
			throw new NotFoundException("user id not found");
		}
		userRepository.delete(userId);
	}

	private void validateUser(User user, Class group) {
		Set<ConstraintViolation<User>> errors = validator.validate(user, group);
		if (!errors.isEmpty())
			throw new ConstraintViolationException(errors);

		if (!user.getLogin().getMail().endsWith("@otto.de"))
			throw new InvalidUserException(user, "business", "only mails by otto allowed");
	}
}

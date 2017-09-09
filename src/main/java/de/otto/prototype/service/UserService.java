package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
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

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class UserService {

	private final UserRepository userRepository;

	private final Validator validator;

	@Autowired
	public UserService(final UserRepository userRepository, final Validator validator) {
		this.userRepository = userRepository;
		this.validator = validator;
	}

	public Stream<User> findAll() {
		return userRepository.streamAll();
	}

	public Optional<User> findOne(final String userId) {
		return Optional.ofNullable(userRepository.findOne(userId));
	}

	public User create(final User user) {
		validateUser(user);
		return userRepository.save(user);
	}

	public User update(final User user, final String eTag) {
		Set<ConstraintViolation<User>> errors = validator.validate(user, User.Existing.class);
		if (!errors.isEmpty())
			throw new ConstraintViolationException(errors);

		User foundUser = userRepository.findOne(user.getId());
		if (foundUser == null)
			throw new NotFoundException("user not found");
		if (!isNullOrEmpty(eTag) && !foundUser.getETag().equals(eTag))
			throw new ConcurrentModificationException("etags aren´t equal");
		validateUser(user);
		return userRepository.save(user);
	}

	public void delete(final String userId) {
		if (userRepository.findOne(userId) == null) {
			throw new NotFoundException("user id not found");
		}
		userRepository.delete(userId);
	}

	private void validateUser(final User userToValidate) {
		if (!userToValidate.getLogin().getMail().endsWith("@otto.de"))
			throw new InvalidUserException(userToValidate, "business", "only mails by otto allowed");
		if (findAll().map(this::getUserWithoutId).anyMatch(user -> user.getETag().equals(getUserWithoutId(userToValidate).getETag())))
			throw new InvalidUserException(userToValidate, "business", "this user does already exist");
	}

	private User getUserWithoutId(User user) {
		return user.toBuilder().id("").build();
	}
}

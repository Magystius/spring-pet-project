package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.Password;
import de.otto.prototype.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.util.Optional;

@Service
public class PasswordService {

	private UserService userService;

	private Validator validator;

	@Autowired
	public PasswordService(UserService userService, Validator validator) {
		this.userService = userService;
		this.validator = validator;
	}

	public User update(String userId, final String password) {
		final Optional<User> userToUpdate;

		if (userId == null || !(userToUpdate = userService.findOne(userId)).isPresent()) {
			throw new NotFoundException("user not found");
		}

		Login login = userToUpdate.get().getLogin().toBuilder().password(password).build();
		final User updatedUser = userToUpdate.get().toBuilder().login(login).build();
		return userService.update(updatedUser);
	}

	public Boolean checkPassword(final String password) {
		return (validator.validate(Password.builder().password(password).build())).isEmpty();
	}
}

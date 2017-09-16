package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.Password;
import de.otto.prototype.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Validator;
import java.util.Optional;

@Service
public class PasswordService {

	private UserService userService;

	private Validator validator;

	@Autowired
	public PasswordService(final UserService userService, final Validator validator) {
		this.userService = userService;
		this.validator = validator;
	}

	public User update(final String userId, final String password) {
		//TODO: optimize this
		if (userId == null || !Optional.ofNullable(userService.findOne(Mono.just(userId)).block()).isPresent()) {
			throw new NotFoundException("user not found");
		}
		final User userToUpdate = userService.findOne(Mono.just(userId)).block();
		Login login = userToUpdate.getLogin().toBuilder().password(password).build();
		final User updatedUser = userToUpdate.toBuilder().login(login).build();
		return userService.update(Mono.just(updatedUser), Mono.empty()).block();
	}

	public Boolean checkPassword(final String password) {
		return (validator.validate(Password.builder().password(password).build())).isEmpty();
	}
}

package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.function.Function;

@Service
public class UserService {

	private final Function<User, User> removeIdFromUser = userToReduce -> userToReduce.toBuilder().id("").build();

	private final UserRepository userRepository;

	private final Validator validator;

	@Autowired
	public UserService(final UserRepository userRepository, final Validator validator) {
		this.userRepository = userRepository;
		this.validator = validator;
	}

	public Flux<User> findAll() {
		return userRepository.findAll();
	}

	public Mono<User> findOne(final Mono<String> userId) {
		return userRepository.findById(userId);
	}

	public Mono<User> create(final Mono<User> userToCreate) {
		return userToCreate.flatMap(this::applyBusinessValidation).flatMap(userRepository::save);
	}

	public Mono<User> update(final Mono<User> userToUpdate, final Mono<String> eTag) {
		return userToUpdate
				.flatMap(this::applyTechnicalValidation)
				.flatMap(user -> userRepository.findById(user.getId()).switchIfEmpty(Mono.error(new NotFoundException("user not found"))))
				.flatMap(user -> eTag
						.flatMap(eTagValue -> user.getETag().equals(eTagValue) ? userToUpdate : Mono.error(new ConcurrentModificationException("etags aren´t equal")))
						.switchIfEmpty(userToUpdate))
				.flatMap(this::applyBusinessValidation)
				.flatMap(userRepository::save);
	}

	public Mono<Void> delete(final Mono<String> userId) {
		return userRepository.findById(userId)
				.switchIfEmpty(Mono.error(new NotFoundException("user not found")))
				.flatMap(userRepository::delete);
	}

	private Mono<User> applyBusinessValidation(final User userToValidate) {
		return Mono.just(userToValidate)
				.flatMap(user -> user.getLogin().getMail().endsWith("@otto.de") ? Mono.just(userToValidate) : Mono.error(new InvalidUserException(user, "business", "only mails by otto allowed")))
				.flatMap(user -> findAll()
						.map(removeIdFromUser)
						.any(userToMatch -> removeIdFromUser.apply(user).getETag().equals(userToMatch.getETag()))
						.flatMap(result -> !result ? Mono.just(userToValidate) : Mono.error(new InvalidUserException(user, "business", "this user does already exist"))));
	}

	private Mono<User> applyTechnicalValidation(final User userToValidate) {
		return Mono.fromCallable(() -> validator.validate(userToValidate, User.Existing.class))
				.subscribeOn(Schedulers.parallel())
				.flatMap(errors -> errors.isEmpty() ? Mono.just(userToValidate) : Mono.error(new ConstraintViolationException(errors)));
	}
}

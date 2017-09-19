package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Password;
import de.otto.prototype.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.Validator;

@Service
public class PasswordService {

    private UserService userService;

    private Validator validator;

    @Autowired
    public PasswordService(final UserService userService, final Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    public Mono<User> update(final Mono<String> userId, final Mono<String> password) {
        return userService.update(userService.findOne(userId)
                        .switchIfEmpty(Mono.error(new NotFoundException("user not found")))
                        .flatMap(user -> password.map(passwordValue -> user.toBuilder().login(user.getLogin().toBuilder().password(passwordValue).build()).build())),
                Mono.empty());
    }

    public Mono<Boolean> checkPassword(final Mono<String> password) {
        return password.flatMap(passwordToValidate -> Mono.fromCallable(() -> validator.validate(Password.builder().password(passwordToValidate).build())))
                .subscribeOn(Schedulers.parallel())
                .flatMap(errors -> Mono.just(errors.isEmpty()));
    }
}

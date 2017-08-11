package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordService {

    private UserService userService;

    @Autowired
    public PasswordService(UserService userService) {
        this.userService = userService;
    }

    public User update(final Long userId, final String password) {
        final Optional<User> userToUpdate;

        if (userId == null || !(userToUpdate = userService.findOne(userId)).isPresent()) {
            throw new NotFoundException("user not found");
        }

        final User updatedUser = userToUpdate.get().toBuilder().password(password).build();
        return userService.update(updatedUser);
    }
}

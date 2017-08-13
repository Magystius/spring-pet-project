package de.otto.prototype.service;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Stream<User> findAll() {
        return userRepository.streamAll();
    }

    public Optional<User> findOne(final Long userId) {
        return Optional.ofNullable(userRepository.findOne(userId));
    }

    public User create(final User user) {
        return userRepository.save(user);
    }

    public User update(final User user) {
        if (userRepository.findOne(user.getId()) == null) {
            throw new NotFoundException("user not found");
        }
        return userRepository.save(user);
    }

    public void delete(final Long userId) {
        if (userRepository.findOne(userId) == null) {
            throw new NotFoundException("user id not found");
        }
        userRepository.delete(userId);
    }
}

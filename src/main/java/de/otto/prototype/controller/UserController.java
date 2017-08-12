package de.otto.prototype.controller;

import de.otto.prototype.model.User;
import de.otto.prototype.model.UserList;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static de.otto.prototype.controller.UserController.URL_USER;
import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(URL_USER)
public class UserController {

    public static final String URL_USER = "/user";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserList> getAll() {
        final Stream<User> allUsers = userService.findAll();
        final UserList listOfUser = UserList.builder().users(allUsers.collect(toList())).build();
        return ok().body(listOfUser);
    }

    @RequestMapping(value = "/{userId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<User> getOne(final @PathVariable("userId") Long userId) {
		final Optional<User> foundUser = userService.findOne(userId);
		return foundUser.map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(final @RequestBody User user) {
        final User persistedUser = userService.create(user);
        return created(URI.create(URL_USER + "/" + persistedUser.getId())).build();
    }

    @RequestMapping(value = "/{userId}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<User> update(final @PathVariable("userId") String userId, final @RequestBody User user) {
        if (!userId.equals(user.getId().toString()))
            return notFound().build();
        return ok(userService.update(user));
    }

    @RequestMapping(value = "/{userId}", method = DELETE)
    public ResponseEntity delete(final @PathVariable("userId") String userId) {
        userService.delete(parseLong(userId));
        return noContent().build();
    }

}

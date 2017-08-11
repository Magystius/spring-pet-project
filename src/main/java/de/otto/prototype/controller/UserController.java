package de.otto.prototype.controller;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.model.UserList;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Stream;

import static de.otto.prototype.controller.UserController.URL_USER;
import static java.lang.Long.parseLong;
import static java.net.URI.create;
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
        Stream<User> allUsers = userService.findAll();
        UserList listOfUser = UserList.builder().users(allUsers.collect(toList())).build();
        return ok().body(listOfUser);
    }

    @RequestMapping(value = "/{userId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getOne(final @PathVariable("userId") String userId) {
        Optional<User> foundUser = userService.findOne(Long.parseLong(userId));
        return foundUser.map(ResponseEntity::ok)
                .orElse(notFound().build());
    }


    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(final @RequestBody User user) {
        User persistedUser = userService.create(user);
        return created(create(URL_USER + "/" + persistedUser.getId())).build();
    }

    @RequestMapping(value = "/{userId}", method = DELETE)
    public ResponseEntity delete(final @PathVariable("userId") String userId) {
        userService.delete(parseLong(userId));
        return noContent().build();
    }


    @ExceptionHandler(InvalidUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void invalidClusterOrderHandler() {
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFoundHandler() {
    }
}

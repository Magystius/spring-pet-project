package de.otto.prototype.controller;

import de.otto.prototype.controller.representation.UserListRepresentation;
import de.otto.prototype.controller.representation.UserRepresentation;
import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.otto.prototype.controller.UserController.URL_USER;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(URL_USER)
@Validated
public class UserController {

    public static final String URL_USER = "/user";

    private UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserListRepresentation> getAll() {
        final List<User> allUsers = userService.findAll().collect(toList());
        if (allUsers.isEmpty())
            return noContent().build();
        final UserListRepresentation listOfUser = UserListRepresentation.builder()
                .users(allUsers)
                .link(linkTo(UserController.class).withSelfRel())
                .link(linkTo(UserController.class).slash(allUsers.get(0)).withRel("start"))
                .total(allUsers.size())
                .build();
        return ok(listOfUser);
    }

    @RequestMapping(value = "/{userId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> getOne(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid")
                                                     @PathVariable("userId") String userId,
                                                     final @RequestHeader(value = "If-None-Match", required = false) String ETagHeader) {
        final Optional<User> foundUser = userService.findOne(userId);
        if (!foundUser.isPresent())
            return notFound().build();
        final User user = foundUser.get();
        final String userETag = user.getETag();
        if (!isNullOrEmpty(ETagHeader) && userETag.equals(ETagHeader))
            return ResponseEntity.status(NOT_MODIFIED).header(ETAG, userETag).build();

        return new ResponseEntity<>(UserRepresentation.builder()
                .user(user)
                .links(determineLinksForUser(user))
                .build(), getETagHeader(user), OK);
    }

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> create(final @Validated(User.New.class) @RequestBody User user) {
        final User persistedUser = userService.create(user);
        return created(linkTo(UserController.class).slash(persistedUser).toUri())
                .body(UserRepresentation.builder()
                        .user(persistedUser)
                        .links(determineLinksForUser(persistedUser))
                        .build());
    }

    @RequestMapping(value = "/{userId}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> update(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("userId") String userId,
                                                     final @Validated(User.Existing.class) @RequestBody User user) {
        if (!userId.equals(user.getId()))
            return notFound().build();
        final User updatedUser = userService.update(user);
        return ok(UserRepresentation.builder()
                .user(updatedUser)
                .links(determineLinksForUser(updatedUser))
                .build());
    }

    @RequestMapping(value = "/{userId}", method = DELETE)
    public ResponseEntity delete(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("userId") String userId) {
        userService.delete(userId);
        return noContent().build();
    }

    private List<Link> determineLinksForUser(final User user) {
        final List<User> listOfUsers = userService.findAll().collect(toList());
        final List<Link> links = new ArrayList<>();
        links.add(linkTo(UserController.class).slash(user).withSelfRel());
        links.add(linkTo(UserController.class).slash(listOfUsers.get(0)).withRel("start"));
        int indexOfUser = listOfUsers.indexOf(user);
        if (indexOfUser > 0)
            links.add(linkTo(UserController.class).slash(listOfUsers.get(indexOfUser - 1)).withRel("prev"));
        if (indexOfUser < (listOfUsers.size() - 1))
            links.add(linkTo(UserController.class).slash(listOfUsers.get(indexOfUser + 1)).withRel("next"));

        return links;
    }

    private MultiValueMap<String, String> getETagHeader(final User user) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(ETAG, user.getETag());
        return headers;
    }

}

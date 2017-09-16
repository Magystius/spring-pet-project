package de.otto.prototype.controller;

import de.otto.prototype.controller.representation.user.UserListEntryRepresentation;
import de.otto.prototype.controller.representation.user.UserListRepresentation;
import de.otto.prototype.controller.representation.user.UserRepresentation;
import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.otto.prototype.controller.UserController.URL_USER;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(URL_USER)
@Validated
public class UserController extends BaseController {

	public static final String URL_USER = "/user";

	private UserService userService;

	@Autowired
	public UserController(final UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<UserListRepresentation> getAll(final @RequestHeader(value = IF_NONE_MATCH, required = false) String ETagHeader) {
		final List<User> allUsers = userService.findAll().toStream().collect(toList());

		if (allUsers.isEmpty())
			return noContent().build();

		final MultiValueMap<String, String> header = getETagHeader(allUsers);
		final String userListETag = header.getFirst(ETAG);
		if (!isNullOrEmpty(ETagHeader) && userListETag.equals(ETagHeader))
			return ResponseEntity.status(NOT_MODIFIED).header(ETAG, userListETag).build();

		final UserListRepresentation listOfUser = UserListRepresentation.builder()
				.users(allUsers.stream().map(UserListEntryRepresentation::userListEntryRepresentationOf).collect(toList()))
				.link(linkTo(UserController.class).withSelfRel())
				.link(linkTo(UserController.class).slash(allUsers.get(0)).withRel("start"))
				.total(allUsers.size())
				.build();

		return new ResponseEntity<>(listOfUser, header, OK);
	}

	@RequestMapping(value = "/{userId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<UserRepresentation> getOne(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid")
													 @PathVariable("userId") String userId,
													 final @RequestHeader(value = IF_NONE_MATCH, required = false) String ETagHeader) {
		final Optional<User> foundUser = Optional.ofNullable(userService.findOne(Mono.just(userId)).block());

		if (!foundUser.isPresent())
			return notFound().build();

		final User user = foundUser.get();
		final String userETag = user.getETag();
		if (!isNullOrEmpty(ETagHeader) && userETag.equals(ETagHeader))
			return ResponseEntity.status(NOT_MODIFIED).header(ETAG, userETag).build();

		return new ResponseEntity<>(UserRepresentation.builder()
				.user(user)
				.links(determineLinks(user, userService.findAll().toStream().collect(toList()), UserController.class))
				.build(), getETagHeader(user), OK);
	}

	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<UserRepresentation> create(final @Validated(User.New.class) @RequestBody User user) {
		final User persistedUser = userService.create(Mono.just(user)).block();
		return created(linkTo(UserController.class).slash(persistedUser).toUri())
				.header(ETAG, persistedUser.getETag())
				.body(UserRepresentation.builder()
						.user(persistedUser)
						.links(determineLinks(persistedUser, userService.findAll().toStream().collect(toList()), UserController.class))
						.build());
	}

	@RequestMapping(value = "/{userId}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<UserRepresentation> update(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("userId") String userId,
													 final @Validated(User.Existing.class) @RequestBody User user,
													 final @RequestHeader(value = IF_MATCH, required = false) String ETagHeader) {
		if (!userId.equals(user.getId()))
			return notFound().build();
		final Mono<String> eTag = ETagHeader == null ? Mono.empty() : Mono.just(ETagHeader);
		final User updatedUser = userService.update(Mono.just(user), eTag).block();
		return new ResponseEntity<>(UserRepresentation.builder()
				.user(updatedUser)
				.links(determineLinks(updatedUser, userService.findAll().toStream().collect(toList()), UserController.class))
				.build(), getETagHeader(updatedUser), OK);
	}

	@RequestMapping(value = "/{userId}", method = DELETE)
	public ResponseEntity delete(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("userId") String userId) {
		userService.delete(Mono.just(userId)).block();
		return noContent().build();
	}
}

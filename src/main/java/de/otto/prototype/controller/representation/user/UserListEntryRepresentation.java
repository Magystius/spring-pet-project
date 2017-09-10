package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.controller.UserController;
import de.otto.prototype.model.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class UserListEntryRepresentation extends ResourceSupport {

	@Singular
	private final List<Link> links;

	@JsonProperty("content")
	private final UserListEntryContent userListEntryContent;

	public static UserListEntryRepresentation userListEntryRepresentationOf(final User user) {
		return UserListEntryRepresentation.builder()
				.link(linkTo(UserController.class).slash(user).withSelfRel())
				.userListEntryContent(UserListEntryContent.builder()
						.id(user.getId())
						.firstName(user.getFirstName())
						.lastName(user.getLastName()).build()).build();
	}

	@Value
	@Builder
	private static class UserListEntryContent {

		private final String id;

		private final String firstName;

		private final String lastName;
	}
}

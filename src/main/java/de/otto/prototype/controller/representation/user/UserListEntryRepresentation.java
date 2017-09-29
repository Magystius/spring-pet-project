package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.controller.UserController;
import de.otto.prototype.model.User;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class UserListEntryRepresentation extends ResourceSupport {

	private final List<Link> links;

	@JsonProperty("content")
	private final UserListEntryContent userListEntryContent;

	@java.beans.ConstructorProperties({"links", "userListEntryContent"})
	UserListEntryRepresentation(List<Link> links, UserListEntryContent userListEntryContent) {
		this.links = links;
		this.userListEntryContent = userListEntryContent;
	}

	public static UserListEntryRepresentation userListEntryRepresentationOf(final User user) {
		return UserListEntryRepresentation.builder()
				.link(linkTo(UserController.class).slash(user).withSelfRel())
				.userListEntryContent(UserListEntryContent.builder()
						.id(user.getId())
						.firstName(user.getFirstName())
						.lastName(user.getLastName()).build()).build();
	}

	public static UserListEntryRepresentationBuilder builder() {
		return new UserListEntryRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public UserListEntryContent getUserListEntryContent() {
		return this.userListEntryContent;
	}

	public String toString() {
		return "UserListEntryRepresentation(links=" + this.getLinks() + ", userListEntryContent=" + this.getUserListEntryContent() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof UserListEntryRepresentation)) return false;
		final UserListEntryRepresentation other = (UserListEntryRepresentation) o;
		if (!other.canEqual(this)) return false;
		if (!super.equals(o)) return false;
		final Object this$links = this.getLinks();
		final Object other$links = other.getLinks();
		if (this$links == null ? other$links != null : !this$links.equals(other$links)) return false;
		final Object this$userListEntryContent = this.getUserListEntryContent();
		final Object other$userListEntryContent = other.getUserListEntryContent();
		return this$userListEntryContent == null ? other$userListEntryContent == null : this$userListEntryContent.equals(other$userListEntryContent);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		final Object $links = this.getLinks();
		result = result * PRIME + ($links == null ? 43 : $links.hashCode());
		final Object $userListEntryContent = this.getUserListEntryContent();
		result = result * PRIME + ($userListEntryContent == null ? 43 : $userListEntryContent.hashCode());
		return result;
	}

	protected boolean canEqual(Object other) {
		return other instanceof UserListEntryRepresentation;
	}

	private static class UserListEntryContent {

		private final String id;

		private final String firstName;

		private final String lastName;

		@java.beans.ConstructorProperties({"id", "firstName", "lastName"})
		UserListEntryContent(String id, String firstName, String lastName) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public static UserListEntryContentBuilder builder() {
			return new UserListEntryContentBuilder();
		}

		public String getId() {
			return this.id;
		}

		public String getFirstName() {
			return this.firstName;
		}

		public String getLastName() {
			return this.lastName;
		}

		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof UserListEntryContent)) return false;
			final UserListEntryContent other = (UserListEntryContent) o;
			final Object this$id = this.getId();
			final Object other$id = other.getId();
			if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
			final Object this$firstName = this.getFirstName();
			final Object other$firstName = other.getFirstName();
			if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName))
				return false;
			final Object this$lastName = this.getLastName();
			final Object other$lastName = other.getLastName();
			return this$lastName == null ? other$lastName == null : this$lastName.equals(other$lastName);
		}

		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $id = this.getId();
			result = result * PRIME + ($id == null ? 43 : $id.hashCode());
			final Object $firstName = this.getFirstName();
			result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
			final Object $lastName = this.getLastName();
			result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
			return result;
		}

		public String toString() {
			return "UserListEntryRepresentation.UserListEntryContent(id=" + this.getId() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ")";
		}

		public static class UserListEntryContentBuilder {
			private String id;
			private String firstName;
			private String lastName;

			UserListEntryContentBuilder() {
			}

			public UserListEntryContent.UserListEntryContentBuilder id(String id) {
				this.id = id;
				return this;
			}

			public UserListEntryContent.UserListEntryContentBuilder firstName(String firstName) {
				this.firstName = firstName;
				return this;
			}

			public UserListEntryContent.UserListEntryContentBuilder lastName(String lastName) {
				this.lastName = lastName;
				return this;
			}

			public UserListEntryContent build() {
				return new UserListEntryContent(id, firstName, lastName);
			}

			public String toString() {
				return "UserListEntryRepresentation.UserListEntryContent.UserListEntryContentBuilder(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ")";
			}
		}
	}

	public static class UserListEntryRepresentationBuilder {
		private ArrayList<Link> links;
		private UserListEntryContent userListEntryContent;

		UserListEntryRepresentationBuilder() {
		}

		public UserListEntryRepresentation.UserListEntryRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public UserListEntryRepresentation.UserListEntryRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public UserListEntryRepresentation.UserListEntryRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public UserListEntryRepresentation.UserListEntryRepresentationBuilder userListEntryContent(UserListEntryContent userListEntryContent) {
			this.userListEntryContent = userListEntryContent;
			return this;
		}

		public UserListEntryRepresentation build() {
			List<Link> links;
			switch (this.links == null ? 0 : this.links.size()) {
				case 0:
					links = java.util.Collections.emptyList();
					break;
				case 1:
					links = java.util.Collections.singletonList(this.links.get(0));
					break;
				default:
					links = java.util.Collections.unmodifiableList(new ArrayList<Link>(this.links));
			}

			return new UserListEntryRepresentation(links, userListEntryContent);
		}

		public String toString() {
			return "UserListEntryRepresentation.UserListEntryRepresentationBuilder(links=" + this.links + ", userListEntryContent=" + this.userListEntryContent + ")";
		}
	}
}

package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.model.User;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRepresentation extends ResourceSupport {

	private final List<Link> links;

	@JsonProperty("content")
	private final User user;

	@java.beans.ConstructorProperties({"links", "user"})
	UserRepresentation(List<Link> links, User user) {
		this.links = links;
		this.user = user;
	}

	public static UserRepresentationBuilder builder() {
		return new UserRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public User getUser() {
		return this.user;
	}

	public String toString() {
		return "UserRepresentation(links=" + this.getLinks() + ", user=" + this.getUser() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof UserRepresentation)) return false;
		final UserRepresentation other = (UserRepresentation) o;
		if (!other.canEqual(this)) return false;
		if (!super.equals(o)) return false;
		final Object this$links = this.getLinks();
		final Object other$links = other.getLinks();
		if (this$links == null ? other$links != null : !this$links.equals(other$links)) return false;
		final Object this$user = this.getUser();
		final Object other$user = other.getUser();
		return this$user == null ? other$user == null : this$user.equals(other$user);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		final Object $links = this.getLinks();
		result = result * PRIME + ($links == null ? 43 : $links.hashCode());
		final Object $user = this.getUser();
		result = result * PRIME + ($user == null ? 43 : $user.hashCode());
		return result;
	}

	protected boolean canEqual(Object other) {
		return other instanceof UserRepresentation;
	}

	public static class UserRepresentationBuilder {
		private ArrayList<Link> links;
		private User user;

		UserRepresentationBuilder() {
		}

		public UserRepresentation.UserRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public UserRepresentation.UserRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public UserRepresentation.UserRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public UserRepresentation.UserRepresentationBuilder user(User user) {
			this.user = user;
			return this;
		}

		public UserRepresentation build() {
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

			return new UserRepresentation(links, user);
		}

		public String toString() {
			return "UserRepresentation.UserRepresentationBuilder(links=" + this.links + ", user=" + this.user + ")";
		}
	}
}

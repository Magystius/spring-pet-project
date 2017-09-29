package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserListRepresentation extends ResourceSupport {

	private final List<Link> links;

	private final Integer total;

	@JsonProperty("content")
	private final List<UserListEntryRepresentation> users;

	@java.beans.ConstructorProperties({"links", "total", "users"})
	UserListRepresentation(List<Link> links, Integer total, List<UserListEntryRepresentation> users) {
		this.links = links;
		this.total = total;
		this.users = users;
	}

	public static UserListRepresentationBuilder builder() {
		return new UserListRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public Integer getTotal() {
		return this.total;
	}

	public List<UserListEntryRepresentation> getUsers() {
		return this.users;
	}

	public String toString() {
		return "UserListRepresentation(links=" + this.getLinks() + ", total=" + this.getTotal() + ", users=" + this.getUsers() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof UserListRepresentation)) return false;
		final UserListRepresentation other = (UserListRepresentation) o;
		if (!other.canEqual(this)) return false;
		if (!super.equals(o)) return false;
		final Object this$links = this.getLinks();
		final Object other$links = other.getLinks();
		if (this$links == null ? other$links != null : !this$links.equals(other$links)) return false;
		final Object this$total = this.getTotal();
		final Object other$total = other.getTotal();
		if (this$total == null ? other$total != null : !this$total.equals(other$total)) return false;
		final Object this$users = this.getUsers();
		final Object other$users = other.getUsers();
		return this$users == null ? other$users == null : this$users.equals(other$users);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		final Object $links = this.getLinks();
		result = result * PRIME + ($links == null ? 43 : $links.hashCode());
		final Object $total = this.getTotal();
		result = result * PRIME + ($total == null ? 43 : $total.hashCode());
		final Object $users = this.getUsers();
		result = result * PRIME + ($users == null ? 43 : $users.hashCode());
		return result;
	}

	protected boolean canEqual(Object other) {
		return other instanceof UserListRepresentation;
	}

	public static class UserListRepresentationBuilder {
		private ArrayList<Link> links;
		private Integer total;
		private ArrayList<UserListEntryRepresentation> users;

		UserListRepresentationBuilder() {
		}

		public UserListRepresentation.UserListRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder total(Integer total) {
			this.total = total;
			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder user(UserListEntryRepresentation user) {
			if (this.users == null) this.users = new ArrayList<UserListEntryRepresentation>();
			this.users.add(user);
			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder users(Collection<? extends UserListEntryRepresentation> users) {
			if (this.users == null) this.users = new ArrayList<UserListEntryRepresentation>();
			this.users.addAll(users);
			return this;
		}

		public UserListRepresentation.UserListRepresentationBuilder clearUsers() {
			if (this.users != null)
				this.users.clear();

			return this;
		}

		public UserListRepresentation build() {
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
			List<UserListEntryRepresentation> users;
			switch (this.users == null ? 0 : this.users.size()) {
				case 0:
					users = java.util.Collections.emptyList();
					break;
				case 1:
					users = java.util.Collections.singletonList(this.users.get(0));
					break;
				default:
					users = java.util.Collections.unmodifiableList(new ArrayList<UserListEntryRepresentation>(this.users));
			}

			return new UserListRepresentation(links, total, users);
		}

		public String toString() {
			return "UserListRepresentation.UserListRepresentationBuilder(links=" + this.links + ", total=" + this.total + ", users=" + this.users + ")";
		}
	}
}

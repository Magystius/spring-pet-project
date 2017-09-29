package de.otto.prototype.controller.representation.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.model.Group;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupRepresentation extends ResourceSupport {

	private final List<Link> links;

	@JsonProperty("content")
	private final Group group;

	@java.beans.ConstructorProperties({"links", "group"})
	GroupRepresentation(List<Link> links, Group group) {
		this.links = links;
		this.group = group;
	}

	public static GroupRepresentationBuilder builder() {
		return new GroupRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public Group getGroup() {
		return this.group;
	}

	public String toString() {
		return "GroupRepresentation(links=" + this.getLinks() + ", group=" + this.getGroup() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GroupRepresentation)) return false;
		final GroupRepresentation other = (GroupRepresentation) o;
		if (!other.canEqual(this)) return false;
		if (!super.equals(o)) return false;
		final Object this$links = this.getLinks();
		final Object other$links = other.getLinks();
		if (this$links == null ? other$links != null : !this$links.equals(other$links)) return false;
		final Object this$group = this.getGroup();
		final Object other$group = other.getGroup();
		return this$group == null ? other$group == null : this$group.equals(other$group);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		final Object $links = this.getLinks();
		result = result * PRIME + ($links == null ? 43 : $links.hashCode());
		final Object $group = this.getGroup();
		result = result * PRIME + ($group == null ? 43 : $group.hashCode());
		return result;
	}

	protected boolean canEqual(Object other) {
		return other instanceof GroupRepresentation;
	}

	public static class GroupRepresentationBuilder {
		private ArrayList<Link> links;
		private Group group;

		GroupRepresentationBuilder() {
		}

		public GroupRepresentation.GroupRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public GroupRepresentation.GroupRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public GroupRepresentation.GroupRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public GroupRepresentation.GroupRepresentationBuilder group(Group group) {
			this.group = group;
			return this;
		}

		public GroupRepresentation build() {
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

			return new GroupRepresentation(links, group);
		}

		public String toString() {
			return "GroupRepresentation.GroupRepresentationBuilder(links=" + this.links + ", group=" + this.group + ")";
		}
	}
}

package de.otto.prototype.controller.representation.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.model.Group;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupListEntryRepresentation extends ResourceSupport {

	private final List<Link> links;

	@JsonProperty("content")
	private final Group group;

	@java.beans.ConstructorProperties({"links", "group"})
	GroupListEntryRepresentation(List<Link> links, Group group) {
		this.links = links;
		this.group = group;
	}

	public static GroupListEntryRepresentationBuilder builder() {
		return new GroupListEntryRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public Group getGroup() {
		return this.group;
	}

	public String toString() {
		return "GroupListEntryRepresentation(links=" + this.getLinks() + ", group=" + this.getGroup() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GroupListEntryRepresentation)) return false;
		final GroupListEntryRepresentation other = (GroupListEntryRepresentation) o;
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
		return other instanceof GroupListEntryRepresentation;
	}

	public static class GroupListEntryRepresentationBuilder {
		private ArrayList<Link> links;
		private Group group;

		GroupListEntryRepresentationBuilder() {
		}

		public GroupListEntryRepresentation.GroupListEntryRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public GroupListEntryRepresentation.GroupListEntryRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public GroupListEntryRepresentation.GroupListEntryRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public GroupListEntryRepresentation.GroupListEntryRepresentationBuilder group(Group group) {
			this.group = group;
			return this;
		}

		public GroupListEntryRepresentation build() {
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

			return new GroupListEntryRepresentation(links, group);
		}

		public String toString() {
			return "GroupListEntryRepresentation.GroupListEntryRepresentationBuilder(links=" + this.links + ", group=" + this.group + ")";
		}
	}
}
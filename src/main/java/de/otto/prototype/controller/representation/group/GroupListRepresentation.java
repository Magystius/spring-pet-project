package de.otto.prototype.controller.representation.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupListRepresentation extends ResourceSupport {

	private final List<Link> links;

	private final Integer total;

	@JsonProperty("content")
	private final List<GroupListEntryRepresentation> groups;

	@java.beans.ConstructorProperties({"links", "total", "groups"})
	GroupListRepresentation(List<Link> links, Integer total, List<GroupListEntryRepresentation> groups) {
		this.links = links;
		this.total = total;
		this.groups = groups;
	}

	public static GroupListRepresentationBuilder builder() {
		return new GroupListRepresentationBuilder();
	}

	public List<Link> getLinks() {
		return this.links;
	}

	public Integer getTotal() {
		return this.total;
	}

	public List<GroupListEntryRepresentation> getGroups() {
		return this.groups;
	}

	public String toString() {
		return "GroupListRepresentation(links=" + this.getLinks() + ", total=" + this.getTotal() + ", groups=" + this.getGroups() + ")";
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GroupListRepresentation)) return false;
		final GroupListRepresentation other = (GroupListRepresentation) o;
		if (!other.canEqual(this)) return false;
		if (!super.equals(o)) return false;
		final Object this$links = this.getLinks();
		final Object other$links = other.getLinks();
		if (this$links == null ? other$links != null : !this$links.equals(other$links)) return false;
		final Object this$total = this.getTotal();
		final Object other$total = other.getTotal();
		if (this$total == null ? other$total != null : !this$total.equals(other$total)) return false;
		final Object this$groups = this.getGroups();
		final Object other$groups = other.getGroups();
		return this$groups == null ? other$groups == null : this$groups.equals(other$groups);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		final Object $links = this.getLinks();
		result = result * PRIME + ($links == null ? 43 : $links.hashCode());
		final Object $total = this.getTotal();
		result = result * PRIME + ($total == null ? 43 : $total.hashCode());
		final Object $groups = this.getGroups();
		result = result * PRIME + ($groups == null ? 43 : $groups.hashCode());
		return result;
	}

	protected boolean canEqual(Object other) {
		return other instanceof GroupListRepresentation;
	}

	public static class GroupListRepresentationBuilder {
		private ArrayList<Link> links;
		private Integer total;
		private ArrayList<GroupListEntryRepresentation> groups;

		GroupListRepresentationBuilder() {
		}

		public GroupListRepresentation.GroupListRepresentationBuilder link(Link link) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.add(link);
			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder links(Collection<? extends Link> links) {
			if (this.links == null) this.links = new ArrayList<Link>();
			this.links.addAll(links);
			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder clearLinks() {
			if (this.links != null)
				this.links.clear();

			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder total(Integer total) {
			this.total = total;
			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder group(GroupListEntryRepresentation group) {
			if (this.groups == null) this.groups = new ArrayList<GroupListEntryRepresentation>();
			this.groups.add(group);
			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder groups(Collection<? extends GroupListEntryRepresentation> groups) {
			if (this.groups == null) this.groups = new ArrayList<GroupListEntryRepresentation>();
			this.groups.addAll(groups);
			return this;
		}

		public GroupListRepresentation.GroupListRepresentationBuilder clearGroups() {
			if (this.groups != null)
				this.groups.clear();

			return this;
		}

		public GroupListRepresentation build() {
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
			List<GroupListEntryRepresentation> groups;
			switch (this.groups == null ? 0 : this.groups.size()) {
				case 0:
					groups = java.util.Collections.emptyList();
					break;
				case 1:
					groups = java.util.Collections.singletonList(this.groups.get(0));
					break;
				default:
					groups = java.util.Collections.unmodifiableList(new ArrayList<GroupListEntryRepresentation>(this.groups));
			}

			return new GroupListRepresentation(links, total, groups);
		}

		public String toString() {
			return "GroupListRepresentation.GroupListRepresentationBuilder(links=" + this.links + ", total=" + this.total + ", groups=" + this.groups + ")";
		}
	}
}

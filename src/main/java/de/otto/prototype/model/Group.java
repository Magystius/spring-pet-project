package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashFunction;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.hash.Hashing.sha256;

@Document
public class Group implements Identifiable<String>, Hashable {

    @Id
    @NotNull(groups = Group.Existing.class, message = "error.id.existing")
    @Null(groups = Group.New.class, message = "error.id.new")
    private final String id;

    @NotEmpty(message = "error.name.empty")
    @Size(min = 3, max = 30, message = "error.name.range")
    private final String name;

    private final boolean vip;

    @NotEmpty(message = "error.userlist.empty")
    private final List<String> userIds;

    @java.beans.ConstructorProperties({"id", "name", "vip", "userIds"})
    Group(String id, String name, boolean vip, List<String> userIds) {
        this.id = id;
        this.name = name;
        this.vip = vip;
        this.userIds = userIds;
    }

    public static GroupBuilder builder() {
        return new GroupBuilder();
    }

    @JsonIgnore
    public String getETag() {
        HashFunction hashFunction = sha256();
        return hashFunction.newHasher()
                .putObject(this, GroupFunnel.INSTANCE)
                .hash().toString();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isVip() {
        return this.vip;
    }

    public List<String> getUserIds() {
        return this.userIds;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Group)) return false;
        final Group other = (Group) o;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        if (this.isVip() != other.isVip()) return false;
        final Object this$userIds = this.getUserIds();
        final Object other$userIds = other.getUserIds();
        return this$userIds == null ? other$userIds == null : this$userIds.equals(other$userIds);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        result = result * PRIME + (this.isVip() ? 79 : 97);
        final Object $userIds = this.getUserIds();
        result = result * PRIME + ($userIds == null ? 43 : $userIds.hashCode());
        return result;
    }

    public String toString() {
        return "Group(id=" + this.getId() + ", name=" + this.getName() + ", vip=" + this.isVip() + ", userIds=" + this.getUserIds() + ")";
    }

    public GroupBuilder toBuilder() {
        return new GroupBuilder().id(this.id).name(this.name).vip(this.vip).userIds(this.userIds);
    }

    public interface Existing extends Default {
    }

    public interface New extends Default {
    }

    public static class GroupBuilder {
        private String id;
        private String name;
        private boolean vip;
        private ArrayList<String> userIds;

        GroupBuilder() {
        }

        public Group.GroupBuilder id(String id) {
            this.id = id;
            return this;
        }

        public Group.GroupBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Group.GroupBuilder vip(boolean vip) {
            this.vip = vip;
            return this;
        }

        public Group.GroupBuilder userId(String userId) {
            if (this.userIds == null) this.userIds = new ArrayList<String>();
            this.userIds.add(userId);
            return this;
        }

        public Group.GroupBuilder userIds(Collection<? extends String> userIds) {
            if (this.userIds == null) this.userIds = new ArrayList<String>();
            this.userIds.addAll(userIds);
            return this;
        }

        public Group.GroupBuilder clearUserIds() {
            if (this.userIds != null)
                this.userIds.clear();

            return this;
        }

        public Group build() {
            List<String> userIds;
            switch (this.userIds == null ? 0 : this.userIds.size()) {
                case 0:
                    userIds = java.util.Collections.emptyList();
                    break;
                case 1:
                    userIds = java.util.Collections.singletonList(this.userIds.get(0));
                    break;
                default:
                    userIds = java.util.Collections.unmodifiableList(new ArrayList<String>(this.userIds));
            }

            return new Group(id, name, vip, userIds);
        }

        public String toString() {
            return "Group.GroupBuilder(id=" + this.id + ", name=" + this.name + ", vip=" + this.vip + ", userIds=" + this.userIds + ")";
        }
    }
}

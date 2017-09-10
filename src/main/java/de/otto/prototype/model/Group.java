package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashFunction;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.util.List;

import static com.google.common.hash.Hashing.sha256;

@Document
@Value
@Builder(toBuilder = true)
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
	@Singular
	private final List<String> userIds;

	@JsonIgnore
	public String getETag() {
		HashFunction hashFunction = sha256();
		return hashFunction.newHasher()
				.putObject(this, GroupFunnel.INSTANCE)
				.hash().toString();
	}

	public interface Existing extends Default {
	}

	public interface New extends Default {
	}
}

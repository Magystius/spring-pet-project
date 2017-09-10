package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class UserListRepresentation extends ResourceSupport {

	@Singular
	private final List<Link> links;

	private final Integer total;

	@Singular
	@JsonProperty("content")
	private final List<UserListEntryRepresentation> users;
}

package de.otto.prototype.controller.representation.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.prototype.model.User;
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
public class UserRepresentation extends ResourceSupport {

	@Singular
	private final List<Link> links;

	@JsonProperty("content")
	private final User user;
}

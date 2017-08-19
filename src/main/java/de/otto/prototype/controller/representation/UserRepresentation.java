package de.otto.prototype.controller.representation;

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
@Builder(toBuilder = true)
public class UserRepresentation extends ResourceSupport {

	@JsonProperty("content")
	private final User user;

	@Singular
	private final List<Link> links;
}
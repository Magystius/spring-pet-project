package de.otto.prototype.controller.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import de.otto.prototype.model.User;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class UserValidationRepresentation {

	User user;

	@Singular
	ImmutableList<UserValidationEntryRepresentation> errors;

}

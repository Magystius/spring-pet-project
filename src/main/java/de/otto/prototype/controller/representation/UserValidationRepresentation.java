package de.otto.prototype.controller.representation;

import de.otto.prototype.model.User;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class UserValidationRepresentation {

	User user;

	@Singular
	List<UserValidationEntryRepresentation> errors;

}

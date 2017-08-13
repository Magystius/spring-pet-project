package de.otto.prototype.controller.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.prototype.model.User;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class UserValidationRepresentation {

	User user;

	@Singular
	List<UserValidationEntryRepresentation> errors;

}

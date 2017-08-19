package de.otto.prototype.controller.representation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserValidationEntryRepresentation {

	String attribute;

	String errorMessage;
}

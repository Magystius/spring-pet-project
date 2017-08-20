package de.otto.prototype.controller.representation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserValidationEntryRepresentation {

	private final String attribute;

	private final String errorMessage;
}

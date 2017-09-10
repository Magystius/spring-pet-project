package de.otto.prototype.controller.representation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationEntryRepresentation {

	private final String attribute;

	private final String errorMessage;
}

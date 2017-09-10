package de.otto.prototype.controller.representation;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ValidationRepresentation<T> {

	private final T data;

	@Singular
	private final List<ValidationEntryRepresentation> errors;
}

package de.otto.prototype.controller.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class UserValidationEntryRepresentation {

	String attribute;

	String errorMessage;
}

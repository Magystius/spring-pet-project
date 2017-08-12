package de.otto.prototype.controller.handlers;

import com.google.common.collect.ImmutableList;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.Locale;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class ControllerValidationHandler {

	private static final Locale LOCALE = LocaleContextHolder.getLocale();

	private final MessageSource msgSource;

	@Autowired
	public ControllerValidationHandler(MessageSource msgSource) {
		this.msgSource = msgSource;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public UserValidationRepresentation processValidationError(ConstraintViolationException exception) {
		//TODO: get the user object from here
		ImmutableList<UserValidationEntryRepresentation> errors = exception.getConstraintViolations().stream().map(constraintViolation -> {
			String msgCode = constraintViolation.getMessage();
			String errObj = constraintViolation.getPropertyPath().toString();
			String msg = msgCode;
			try {
				msg = msgSource.getMessage(msgCode, null, LOCALE);
			} catch (NoSuchMessageException ignored) {
			}
			return UserValidationEntryRepresentation.builder().attribute(errObj).errorMessage(msg).build();
		}).collect(collectingAndThen(toList(), ImmutableList::copyOf));

		return UserValidationRepresentation.builder().errors(errors).build();
	}
}

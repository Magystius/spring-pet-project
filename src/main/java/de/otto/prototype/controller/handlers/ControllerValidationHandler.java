package de.otto.prototype.controller.handlers;

import com.google.common.collect.ImmutableList;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.Locale;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

//TODO: add user object to response
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
		ImmutableList<UserValidationEntryRepresentation> errors = exception.getConstraintViolations().stream()
				.map(constraintViolation -> getUserValidationEntryRepresentation(constraintViolation.getMessage(), constraintViolation.getPropertyPath().toString()))
				.collect(collectingAndThen(toList(), ImmutableList::copyOf));

		return UserValidationRepresentation.builder().errors(errors).build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public UserValidationRepresentation processValidationError(MethodArgumentNotValidException exception) {
		ImmutableList<UserValidationEntryRepresentation> errors = exception.getBindingResult().getAllErrors().stream()
				.map(objectError -> getUserValidationEntryRepresentation(objectError.getDefaultMessage(), objectError.getObjectName()))
				.collect(collectingAndThen(toList(), ImmutableList::copyOf));

		return UserValidationRepresentation.builder().errors(errors).build();
	}

	private UserValidationEntryRepresentation getUserValidationEntryRepresentation(String msgCode, String errObj) {
		String msg = msgCode;
		try {
			msg = msgSource.getMessage(msgCode, null, LOCALE);
		} catch (NoSuchMessageException ignored) {
		}
		return UserValidationEntryRepresentation.builder().attribute(errObj).errorMessage(msg).build();
	}

	@ExceptionHandler(InvalidUserException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public UserValidationRepresentation processValidationError(InvalidUserException exception) {
		UserValidationEntryRepresentation error = UserValidationEntryRepresentation.builder().attribute(exception.getErrorCause()).errorMessage(exception.getErrorMsg()).build();
		return UserValidationRepresentation.builder().error(error).build();
	}

	//TODO: remove this -> only here because unit test is broken...
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public void processValidationError() {
	}
}

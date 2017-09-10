package de.otto.prototype.controller.handlers;

import com.google.common.collect.ImmutableList;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidGroupException;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.User;
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
import static org.springframework.http.HttpStatus.*;

//TODO: add user object to response
@ControllerAdvice
public class ControllerValidationHandler {

	private static final Locale LOCALE = LocaleContextHolder.getLocale();

	private final MessageSource msgSource;

	@Autowired
	public ControllerValidationHandler(final MessageSource msgSource) {
		this.msgSource = msgSource;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ValidationRepresentation processValidationError(final ConstraintViolationException exception) {
		ImmutableList<ValidationEntryRepresentation> errors = exception.getConstraintViolations().stream()
				.map(constraintViolation -> getValidationEntryRepresentation(constraintViolation.getMessage(), constraintViolation.getPropertyPath().toString()))
				.collect(collectingAndThen(toList(), ImmutableList::copyOf));

		return ValidationRepresentation.builder().errors(errors).build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ValidationRepresentation processValidationError(final MethodArgumentNotValidException exception) {
		ImmutableList<ValidationEntryRepresentation> errors = exception.getBindingResult().getAllErrors().stream()
				.map(objectError -> getValidationEntryRepresentation(objectError.getDefaultMessage(), objectError.getObjectName()))
				.collect(collectingAndThen(toList(), ImmutableList::copyOf));

		return ValidationRepresentation.builder().errors(errors).build();
	}

	@ExceptionHandler(InvalidUserException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ValidationRepresentation<User> processValidationError(final InvalidUserException exception) {
		ValidationEntryRepresentation error = ValidationEntryRepresentation.builder().attribute(exception.getErrorCause()).errorMessage(exception.getErrorMsg()).build();
		return ValidationRepresentation.<User>builder().error(error).build();
	}

	@ExceptionHandler(InvalidGroupException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ValidationRepresentation<Group> processValidationError(final InvalidGroupException exception) {
		ValidationEntryRepresentation error = ValidationEntryRepresentation.builder().attribute(exception.getErrorCause()).errorMessage(exception.getErrorMsg()).build();
		return ValidationRepresentation.<Group>builder().error(error).build();
	}

	//TODO: remove this -> only here because unit test is broken...

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(NOT_FOUND)
	public void processValidationError() {
		//do nothing
	}

	@ExceptionHandler(ConcurrentModificationException.class)
	@ResponseStatus(PRECONDITION_FAILED)
	public void proccessConcurrentModificationError() {
		//do nothing
	}

	private ValidationEntryRepresentation getValidationEntryRepresentation(final String msgCode, final String errObj) {
		String msg = msgCode;
		try {
			msg = msgSource.getMessage(msgCode, null, LOCALE);
		} catch (NoSuchMessageException ignored) {
		}
		return ValidationEntryRepresentation.builder().attribute(errObj).errorMessage(msg).build();
	}
}

package de.otto.prototype.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
public class NotFoundException extends RuntimeException {
	public NotFoundException(String msg) {
		super(msg);
	}
}

package de.otto.prototype.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(value = BAD_REQUEST)
public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String msg) {
        super(msg);
    }
}

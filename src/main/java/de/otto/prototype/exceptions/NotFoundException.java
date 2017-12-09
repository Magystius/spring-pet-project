package de.otto.prototype.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(final String msg) {
        super(msg);
    }
}

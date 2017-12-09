package de.otto.prototype.exceptions;

import de.otto.prototype.model.User;

public class InvalidUserException extends RuntimeException {

    private final User user;
    private final String errorCause;
    private final String errorMsg;

    public InvalidUserException(final User user, final String errorCause, final String errorMsg) {
        super(errorMsg);
        this.user = user;
        this.errorCause = errorCause;
        this.errorMsg = errorMsg;
    }

    public User getUser() {
        return this.user;
    }

    public String getErrorCause() {
        return this.errorCause;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }
}

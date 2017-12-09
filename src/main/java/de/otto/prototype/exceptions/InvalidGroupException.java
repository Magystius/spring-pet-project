package de.otto.prototype.exceptions;

import de.otto.prototype.model.Group;

public class InvalidGroupException extends RuntimeException {

    private final Group group;
    private final String errorCause;
    private final String errorMsg;

    public InvalidGroupException(final Group group, final String errorCause, final String errorMsg) {
        super(errorMsg);
        this.group = group;
        this.errorCause = errorCause;
        this.errorMsg = errorMsg;
    }

    public Group getGroup() {
        return this.group;
    }

    public String getErrorCause() {
        return this.errorCause;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }
}

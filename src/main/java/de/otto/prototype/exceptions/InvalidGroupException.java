package de.otto.prototype.exceptions;

import de.otto.prototype.model.Group;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(value = BAD_REQUEST)
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

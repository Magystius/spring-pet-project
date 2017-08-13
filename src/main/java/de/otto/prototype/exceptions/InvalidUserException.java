package de.otto.prototype.exceptions;

import de.otto.prototype.model.User;
import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@ResponseStatus(value = BAD_REQUEST)
public class InvalidUserException extends RuntimeException {

    private User user;
    private String errorCause;
    private String errorMsg;

    public InvalidUserException(User user, String errorCause, String errorMsg) {
        super(errorMsg);
        this.user = user;
        this.errorCause = errorCause;
        this.errorMsg = errorMsg;
    }
}

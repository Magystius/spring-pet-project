package de.otto.prototype.model;

import de.otto.prototype.validation.SecurePassword;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Value
@Builder(toBuilder = true)
public class Login {

	@NotEmpty(message = "error.mail.empty")
	@Email(message = "error.mail.invalid")
	private final String mail;

	@NotEmpty(message = "error.password.empty")
	@SecurePassword(pattern = ".*")
	private final String password;
}

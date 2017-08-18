package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.prototype.validation.SecurePassword;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class Login {

	@NotEmpty(message = "error.mail.empty")
	@Email(message = "error.mail.invalid")
	private String mail;

	@NotEmpty(message = "error.password.empty")
	@SecurePassword(pattern = ".*")
	private String password;
}

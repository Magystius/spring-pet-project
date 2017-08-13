package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.prototype.validation.SecurePassword;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class Login {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@NotNull(groups = User.Existing.class, message = "error.id.existing")
	@Null(groups = User.New.class, message = "error.id.new")
	private Long id;

	@NotEmpty(message = "error.mail.empty")
	@Email(message = "error.mail.invalid")
	private String mail;

	@NotEmpty(message = "error.password.empty")
	@SecurePassword(pattern = ".*")
	private String password;
}

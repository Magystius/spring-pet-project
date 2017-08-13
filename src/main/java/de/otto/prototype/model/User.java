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
import javax.validation.constraints.*;
import javax.validation.groups.Default;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@NotNull(groups = Existing.class, message = "error.id.existing")
	@Null(groups = New.class, message = "error.id.new")
	private Long id;

	@NotEmpty(message = "error.name.empty")
	@Size(min = 3, max = 30, message = "error.name.range")
	private String firstName;
	@Size(min = 3, max = 30, message = "error.name.range")
	private String secondName;
	@NotEmpty(message = "error.name.empty")
	@Size(min = 3, max = 30, message = "error.name.range")
	private String lastName;
	@NotNull(message = "error.age.empty")
	@Min(value = 18, message = "error.age.young")
	@Max(value = 150, message = "error.age.old")
	private int age;

	private boolean vip;
	@NotEmpty(message = "error.mail.empty")
	@Email(message = "error.mail.invalid")
	private String mail;
	@NotEmpty(message = "error.password.empty")
	@SecurePassword(pattern = ".*")
	private String password;

	public interface Existing extends Default {
	}

	public interface New extends Default {
	}
}

package de.otto.prototype.model;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;

import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE;

@Document
@Value
@Builder(toBuilder = true)
public class User {

	@Id
	@NotNull(groups = Existing.class, message = "error.id.existing")
	@Null(groups = New.class, message = "error.id.new")
	private String id;

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

	@Valid
	private Login login;

	@SafeHtml(whitelistType = NONE, message = "error.bio.invalid")
	private String bio;

	public interface Existing extends Default {
	}

	public interface New extends Default {
	}
}

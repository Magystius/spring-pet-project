package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.prototype.validation.SecurePasswordConstraint;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
	private Long id;

    @NotEmpty
    @Size(min = 3, max = 30)
    private String firstName;
    @Size(min = 3, max = 30)
    private String secondName;
    @NotEmpty
    @Size(min = 3, max = 30)
    private String lastName;
    @NotNull
    @Min(18)
    @Max(150)
    private int age;

	private boolean vip;
    @NotEmpty
    @Email
    private String mail;
    @NotEmpty
    @SecurePasswordConstraint
    private String password;
}

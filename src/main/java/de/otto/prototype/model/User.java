package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashFunction;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;

import static com.google.common.hash.Hashing.sha256;
import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE;

@Document
public class User implements Identifiable<String>, Hashable {

    private static final int AGE_YOUNGEST = 18;
    private static final int AGE_OLDEST = 150;

	@Id
	@NotNull(groups = Existing.class, message = "error.id.existing")
	@Null(groups = New.class, message = "error.id.new")
	private final String id;

	@NotEmpty(message = "error.name.empty")
	@Size(min = 3, max = 30, message = "error.name.range")
	private final String firstName;

	@Size(min = 3, max = 30, message = "error.name.range")
	private final String secondName;

	@NotEmpty(message = "error.name.empty")
	@Size(min = 3, max = 30, message = "error.name.range")
	private final String lastName;

	@NotNull(message = "error.age.empty")
	@Min(value = AGE_YOUNGEST, message = "error.age.young")
	@Max(value = AGE_OLDEST, message = "error.age.old")
	private final int age;

	private final boolean vip;

	@Valid
	private final Login login;

	@SafeHtml(whitelistType = NONE, message = "error.bio.invalid")
	private final String bio;

	@java.beans.ConstructorProperties({"id", "firstName", "secondName", "lastName", "age", "vip", "login", "bio"})
	User(String id, String firstName, String secondName, String lastName, int age, boolean vip, Login login, String bio) {
		this.id = id;
		this.firstName = firstName;
		this.secondName = secondName;
		this.lastName = lastName;
		this.age = age;
		this.vip = vip;
		this.login = login;
		this.bio = bio;
	}

	public static UserBuilder builder() {
		return new UserBuilder();
	}

	@JsonIgnore
	public String getETag() {
		HashFunction hashFunction = sha256();
		return hashFunction.newHasher()
				.putObject(this, UserFunnel.INSTANCE)
				.hash().toString();
	}

	public String getId() {
		return this.id;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getSecondName() {
		return this.secondName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public int getAge() {
		return this.age;
	}

	public boolean isVip() {
		return this.vip;
	}

	public Login getLogin() {
		return this.login;
	}

	public String getBio() {
		return this.bio;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof User)) return false;
		final User other = (User) o;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$firstName = this.getFirstName();
		final Object other$firstName = other.getFirstName();
		if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName)) return false;
		final Object this$secondName = this.getSecondName();
		final Object other$secondName = other.getSecondName();
		if (this$secondName == null ? other$secondName != null : !this$secondName.equals(other$secondName))
			return false;
		final Object this$lastName = this.getLastName();
		final Object other$lastName = other.getLastName();
		if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName)) return false;
		if (this.getAge() != other.getAge()) return false;
		if (this.isVip() != other.isVip()) return false;
		final Object this$login = this.getLogin();
		final Object other$login = other.getLogin();
		if (this$login == null ? other$login != null : !this$login.equals(other$login)) return false;
		final Object this$bio = this.getBio();
		final Object other$bio = other.getBio();
		return this$bio == null ? other$bio == null : this$bio.equals(other$bio);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $firstName = this.getFirstName();
		result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
		final Object $secondName = this.getSecondName();
		result = result * PRIME + ($secondName == null ? 43 : $secondName.hashCode());
		final Object $lastName = this.getLastName();
		result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
		result = result * PRIME + this.getAge();
		result = result * PRIME + (this.isVip() ? 79 : 97);
		final Object $login = this.getLogin();
		result = result * PRIME + ($login == null ? 43 : $login.hashCode());
		final Object $bio = this.getBio();
		result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
		return result;
	}

	public String toString() {
		return "User(id=" + this.getId() + ", firstName=" + this.getFirstName() + ", secondName=" + this.getSecondName() + ", lastName=" + this.getLastName() + ", age=" + this.getAge() + ", vip=" + this.isVip() + ", login=" + this.getLogin() + ", bio=" + this.getBio() + ")";
	}

	public UserBuilder toBuilder() {
		return new UserBuilder().id(this.id).firstName(this.firstName).secondName(this.secondName).lastName(this.lastName).age(this.age).vip(this.vip).login(this.login).bio(this.bio);
	}

	public interface Existing extends Default {
	}

	public interface New extends Default {
	}

	public static class UserBuilder {
		private String id;
		private String firstName;
		private String secondName;
		private String lastName;
		private int age;
		private boolean vip;
		private Login login;
		private String bio;

		UserBuilder() {
		}

		public User.UserBuilder id(String id) {
			this.id = id;
			return this;
		}

		public User.UserBuilder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public User.UserBuilder secondName(String secondName) {
			this.secondName = secondName;
			return this;
		}

		public User.UserBuilder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public User.UserBuilder age(int age) {
			this.age = age;
			return this;
		}

		public User.UserBuilder vip(boolean vip) {
			this.vip = vip;
			return this;
		}

		public User.UserBuilder login(Login login) {
			this.login = login;
			return this;
		}

		public User.UserBuilder bio(String bio) {
			this.bio = bio;
			return this;
		}

		public User build() {
			return new User(id, firstName, secondName, lastName, age, vip, login, bio);
		}

		public String toString() {
			return "User.UserBuilder(id=" + this.id + ", firstName=" + this.firstName + ", secondName=" + this.secondName + ", lastName=" + this.lastName + ", age=" + this.age + ", vip=" + this.vip + ", login=" + this.login + ", bio=" + this.bio + ")";
		}
	}
}

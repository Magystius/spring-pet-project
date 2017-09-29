package de.otto.prototype.model;

import de.otto.prototype.validation.SecurePassword;

public class Password {

	@SecurePassword(pattern = ".*")
	private final String password;

	@java.beans.ConstructorProperties({"password"})
	Password(String password) {
		this.password = password;
	}

	public static PasswordBuilder builder() {
		return new PasswordBuilder();
	}

	public String getPassword() {
		return this.password;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Password)) return false;
		final Password other = (Password) o;
		final Object this$password = this.getPassword();
		final Object other$password = other.getPassword();
		return this$password == null ? other$password == null : this$password.equals(other$password);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $password = this.getPassword();
		result = result * PRIME + ($password == null ? 43 : $password.hashCode());
		return result;
	}

	public String toString() {
		return "Password(password=" + this.getPassword() + ")";
	}

	public static class PasswordBuilder {
		private String password;

		PasswordBuilder() {
		}

		public Password.PasswordBuilder password(String password) {
			this.password = password;
			return this;
		}

		public Password build() {
			return new Password(password);
		}

		public String toString() {
			return "Password.PasswordBuilder(password=" + this.password + ")";
		}
	}
}

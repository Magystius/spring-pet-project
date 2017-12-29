package de.otto.prototype.model;

import de.otto.prototype.validation.SecurePassword;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class Login {

    @NotEmpty(message = "error.mail.empty")
    @Email(message = "error.mail.invalid")
    private final String mail;

    @NotEmpty(message = "error.password.empty")
    @SecurePassword
    private final String password;

    @java.beans.ConstructorProperties({"mail", "password"})
    Login(String mail, String password) {
        this.mail = mail;
        this.password = password;
    }

    public static LoginBuilder builder() {
        return new LoginBuilder();
    }

    public String getMail() {
        return this.mail;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Login)) return false;
        final Login other = (Login) o;
        final Object this$mail = this.getMail();
        final Object other$mail = other.getMail();
        if (this$mail == null ? other$mail != null : !this$mail.equals(other$mail)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        return this$password == null ? other$password == null : this$password.equals(other$password);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $mail = this.getMail();
        result = result * PRIME + ($mail == null ? 43 : $mail.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        return result;
    }

    public String toString() {
        return "Login(mail=" + this.getMail() + ", password=" + this.getPassword() + ")";
    }

    public LoginBuilder toBuilder() {
        return new LoginBuilder().mail(this.mail).password(this.password);
    }

    public static class LoginBuilder {
        private String mail;
        private String password;

        LoginBuilder() {
        }

        public Login.LoginBuilder mail(String mail) {
            this.mail = mail;
            return this;
        }

        public Login.LoginBuilder password(String password) {
            this.password = password;
            return this;
        }

        public Login build() {
            return new Login(mail, password);
        }

        public String toString() {
            return "Login.LoginBuilder(mail=" + this.mail + ", password=" + this.password + ")";
        }
    }
}

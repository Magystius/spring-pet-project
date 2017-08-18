package de.otto.prototype.controller;

import de.otto.prototype.model.User;
import de.otto.prototype.service.PasswordService;
import de.otto.prototype.validation.SecurePassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Validated
public class PasswordController {

	public static final String URL_RESET_PASSWORD = "/resetpassword";
	static final String URL_CHECK_PASSWORD = "/checkpassword";

	private PasswordService passwordService;

	@Autowired
	public PasswordController(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	@RequestMapping(value = URL_RESET_PASSWORD, method = POST, consumes = TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<User> updateUserPassword(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @RequestParam("userId") String id,
												   final @SecurePassword(pattern = ".*") @RequestBody String password) {
		final User updatedUser = passwordService.update(id, password);
		return ok(updatedUser);
	}

	@RequestMapping(value = URL_CHECK_PASSWORD, method = POST, consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	public ResponseEntity<String> checkPassword(final @RequestBody String password) {
		final Boolean validationResult = passwordService.checkPassword(password);
		return ok(Boolean.toString(validationResult));
	}

}

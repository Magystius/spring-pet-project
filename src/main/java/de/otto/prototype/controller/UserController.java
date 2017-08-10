package de.otto.prototype.controller;

import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@RestController
public class UserController {

	private UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	@RequestMapping("/user")
	public ResponseEntity<String> getUser() {
		Stream<User> allUsers = userService.findAll();
		String stringifiedUsers = allUsers
				.map(User::toString)
				.collect(joining("; "));
		return ResponseEntity.ok().body(stringifiedUsers);
	}
}

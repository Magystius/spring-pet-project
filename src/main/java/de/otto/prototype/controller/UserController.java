package de.otto.prototype.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@RequestMapping("/user")
	public ResponseEntity<String> getUser() {
		return ResponseEntity.ok().body("I am a user");
	}
}

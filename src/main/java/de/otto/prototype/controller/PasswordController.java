package de.otto.prototype.controller;

import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.User;
import de.otto.prototype.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.otto.prototype.controller.PasswordController.URL_PASSWORD;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(URL_PASSWORD)
public class PasswordController {

    public static final String URL_PASSWORD = "/resetpassword";

    private PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @RequestMapping(method = POST, consumes = TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateUserPassword(final @RequestParam("id") String id,
                                                   final @RequestBody String password) {
        final User updatedUser = passwordService.update(Long.parseLong(id), password);
        return ok(updatedUser);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFoundHandler() {
    }
}

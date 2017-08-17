package de.otto.prototype.model;

import de.otto.prototype.validation.SecurePassword;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Password {

    @SecurePassword(pattern = ".*")
    private final String password;
}

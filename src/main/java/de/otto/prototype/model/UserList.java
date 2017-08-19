package de.otto.prototype.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class UserList {

    @Singular
	private final List<User> users;
}

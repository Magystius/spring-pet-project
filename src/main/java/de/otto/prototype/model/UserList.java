package de.otto.prototype.model;

import lombok.*;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class UserList {

    @Singular
    List<User> users;
}

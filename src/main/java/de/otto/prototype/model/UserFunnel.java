package de.otto.prototype.model;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

import static com.google.common.base.Charsets.UTF_8;

public enum UserFunnel implements Funnel<User> {
    INSTANCE;

    @ParametersAreNonnullByDefault
    public void funnel(final User user, final PrimitiveSink into) {
        into
                .putString(user.getId(), UTF_8)
                .putString(user.getFirstName(), UTF_8)
                .putString(Optional.ofNullable(user.getSecondName()).orElse(""), UTF_8)
                .putString(user.getLastName(), UTF_8)
                .putInt(user.getAge())
                .putBoolean(user.isVip())
                .putString(user.getLogin().getMail(), UTF_8)
                .putString(user.getLogin().getPassword(), UTF_8)
                .putString(Optional.ofNullable(user.getBio()).orElse(""), UTF_8);
    }
}
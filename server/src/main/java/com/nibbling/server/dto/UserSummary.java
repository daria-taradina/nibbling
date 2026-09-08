package com.nibbling.server.dto;

import com.nibbling.server.entity.User;

import java.util.UUID;

/**
 * What we're willing to hand back to the client about a user — never the
 * entity itself, so there's no chance of the password hash leaking through
 * a stray Jackson serialization.
 */
public class UserSummary {

    private final UUID id;
    private final String username;
    private final boolean emailVerified;

    public UserSummary(UUID id, String username, boolean emailVerified) {
        this.id = id;
        this.username = username;
        this.emailVerified = emailVerified;
    }

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.isEmailVerified());
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}

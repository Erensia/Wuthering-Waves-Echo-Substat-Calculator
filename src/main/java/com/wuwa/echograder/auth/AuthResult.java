package com.wuwa.echograder.auth;

import java.util.UUID;

public record AuthResult(UUID id, String username) {

    public static AuthResult from(UserAccount user) {
        return new AuthResult(user.getId(), user.getUsername());
    }
}

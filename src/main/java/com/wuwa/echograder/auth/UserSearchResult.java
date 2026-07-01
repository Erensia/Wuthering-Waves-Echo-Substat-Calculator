package com.wuwa.echograder.auth;

import java.time.Instant;

public record UserSearchResult(String username, Instant joinedAt) {

    public static UserSearchResult from(UserAccount user) {
        return new UserSearchResult(user.getUsername(), user.getCreatedAt());
    }
}

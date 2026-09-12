package com.nibbling.server.dto;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresInMs;
    private final UserSummary user;

    public AuthResponse(String accessToken, long expiresInMs, UserSummary user) {
        this.accessToken = accessToken;
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public UserSummary getUser() {
        return user;
    }
}

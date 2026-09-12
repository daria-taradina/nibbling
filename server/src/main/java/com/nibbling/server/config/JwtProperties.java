package com.nibbling.server.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bound from jwt.secret / jwt.expiration-ms. Annotated as a @Component
 * directly (rather than relying on @ConfigurationPropertiesScan being set
 * on the main application class) so this works regardless of what the rest
 * of the app does.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @Positive
    private long expirationMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}

package com.nibbling.server.security;

import com.nibbling.server.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    // 256 bits, the minimum key size HS256 requires.
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties properties;
    private SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        byte[] secretBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes (256 bits) for HS256. " +
                    "Generate one with: openssl rand -base64 32"
            );
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long currentExpirationMs() {
        return properties.getExpirationMs();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

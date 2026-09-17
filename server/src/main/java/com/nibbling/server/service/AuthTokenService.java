package com.nibbling.server.service;

import com.nibbling.server.entity.AuthToken;
import com.nibbling.server.entity.TokenType;
import com.nibbling.server.entity.User;
import com.nibbling.server.exception.InvalidTokenException;
import com.nibbling.server.repository.AuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

// Creates and checks the one-time tokens that go inside emailed links.
@Service
public class AuthTokenService {

    private static final Duration VERIFICATION_LIFETIME = Duration.ofHours(24);
    private static final Duration RESET_LIFETIME = Duration.ofMinutes(30);

    private final AuthTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(AuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // Returns the RAW token (for the email link). Only its hash is saved.
    // Any older unused token of the same type is cancelled, so only the
    // newest link works.
    @Transactional
    public String createToken(User user, TokenType type) {
        Instant now = Instant.now();
        tokenRepository.findByUserAndTypeAndUsedAtIsNull(user, type)
                .forEach(old -> old.markUsed(now));

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Duration lifetime = type == TokenType.PASSWORD_RESET ? RESET_LIFETIME : VERIFICATION_LIFETIME;
        tokenRepository.save(new AuthToken(user, hash(rawToken), type, now.plus(lifetime)));
        return rawToken;
    }

    // Checks the token and marks it used. Returns the user it belongs to.
    @Transactional
    public User consumeToken(String rawToken, TokenType type) {
        Instant now = Instant.now();
        AuthToken token = tokenRepository.findByTokenHashAndType(hash(rawToken), type)
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new InvalidTokenException(
                        type == TokenType.PASSWORD_RESET
                                ? "This reset link is invalid or has expired"
                                : "This verification link is invalid or has expired"));

        token.markUsed(now);
        return token.getUser();
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

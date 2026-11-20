package com.nibbling.server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

// A single-use link token for email verification or password reset.
// Only a SHA-256 hash of the token is stored, so a leaked database row
// can't be turned back into a working link.
@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private TokenType type;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Null until the link is clicked. Once set, the token can't be reused.
    @Column(name = "used_at")
    private Instant usedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    protected AuthToken() {
        // required by JPA
    }

    public AuthToken(User user, String tokenHash, TokenType type, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(Instant now) {
        this.usedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public TokenType getType() {
        return type;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}

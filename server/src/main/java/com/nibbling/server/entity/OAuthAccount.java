package com.nibbling.server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

// Links a Nibbling user to a GitHub or Discord account.
// One user can have several linked providers.
@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_provider_user",
                columnNames = {"provider", "provider_user_id"}
        )
)
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // "github" or "discord"
    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    // The provider's own id for this person (never changes, unlike usernames).
    @Column(name = "provider_user_id", length = 100, nullable = false)
    private String providerUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    protected OAuthAccount() {
        // required by JPA
    }

    public OAuthAccount(User user, String provider, String providerUserId) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }
}

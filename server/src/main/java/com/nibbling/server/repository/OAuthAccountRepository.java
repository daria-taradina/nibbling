package com.nibbling.server.repository;

import com.nibbling.server.entity.OAuthAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}

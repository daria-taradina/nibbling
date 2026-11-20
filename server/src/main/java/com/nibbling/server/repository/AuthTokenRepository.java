package com.nibbling.server.repository;

import com.nibbling.server.entity.AuthToken;
import com.nibbling.server.entity.TokenType;
import com.nibbling.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findByTokenHashAndType(String tokenHash, TokenType type);

    List<AuthToken> findByUserAndTypeAndUsedAtIsNull(User user, TokenType type);
}

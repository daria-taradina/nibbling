package com.nibbling.server.service;

import com.nibbling.server.entity.OAuthAccount;
import com.nibbling.server.entity.User;
import com.nibbling.server.repository.OAuthAccountRepository;
import com.nibbling.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

@Service
public class OAuthAccountService {

    private static final int MAX_USERNAME_LENGTH = 30;

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public OAuthAccountService(OAuthAccountRepository oAuthAccountRepository, UserRepository userRepository) {
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;
    }

    /**
     * 1. Already linked?                      -> that user.
     * 2. A user with the same verified email? -> link to them.
     * 3. Otherwise                            -> create a new user.
     */
    @Transactional
    public User findOrCreateUser(String provider, String providerUserId, String email,
                                 boolean emailVerified, String usernameHint) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider did not return a user id");
        }

        Optional<OAuthAccount> linked =
                oAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (linked.isPresent()) {
            return linked.get().getUser();
        }

        // Only trust an email the provider has verified. Otherwise anyone could
        // add someone else's address to a GitHub/Discord account and take over
        // the matching Nibbling account.
        if (email == null || email.isBlank() || !emailVerified) {
            throw new OAuthEmailRequiredException();
        }
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    if (!existing.isEmailVerified()) {
                        // Someone signed up with this email but never proved they own it.
                        // The provider just proved the real owner is here, so remove the
                        // unproven password to stop a pre-registered account being hijacked.
                        existing.setPassword(null);
                        existing.setEmailVerified(true);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User created = new User(uniqueUsername(usernameHint), normalizedEmail, null);
                    created.setEmailVerified(true);
                    return userRepository.save(created);
                });

        oAuthAccountRepository.save(new OAuthAccount(user, provider, providerUserId));
        return user;
    }

    // Turns "Cool.Coder-99" into "coolcoder99", then adds digits if taken.
    private String uniqueUsername(String hint) {
        String base = hint == null ? "" : hint.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        if (base.length() < 3) {
            base = "nibbler";
        }
        base = base.substring(0, Math.min(base.length(), MAX_USERNAME_LENGTH - 5));

        String candidate = base;
        for (int attempt = 0; userRepository.existsByUsername(candidate); attempt++) {
            if (attempt >= 20) {
                throw new IllegalStateException("Could not generate a unique username");
            }
            candidate = base + "_" + (1000 + random.nextInt(9000));
        }
        return candidate;
    }

    // The provider didn't share a verified email, which every account needs.
    public static class OAuthEmailRequiredException extends RuntimeException {
        public OAuthEmailRequiredException() {
            super("A verified email is required to sign in");
        }
    }
}

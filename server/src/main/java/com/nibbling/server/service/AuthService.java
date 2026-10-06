package com.nibbling.server.service;

import com.nibbling.server.dto.AuthResponse;
import com.nibbling.server.dto.LoginRequest;
import com.nibbling.server.dto.RegisterRequest;
import com.nibbling.server.dto.UserSummary;
import com.nibbling.server.entity.TokenType;
import com.nibbling.server.entity.User;
import com.nibbling.server.exception.EmailAlreadyExistsException;
import com.nibbling.server.exception.InvalidCredentialsException;
import com.nibbling.server.exception.UsernameAlreadyExistsException;
import com.nibbling.server.repository.UserRepository;
import com.nibbling.server.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthTokenService authTokenService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AuthTokenService authTokenService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.authTokenService = authTokenService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().toLowerCase();
        String email = request.getEmail().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // NEW: send the verification link. The user is still logged in right
        // away; the app can show a "verify your email" banner until they click it.
        sendVerification(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    // The signed-in user's public summary (for the navbar, etc). The JWT only
    // carries id + email, so the username has to come from the database.
    @Transactional(readOnly = true)
    public UserSummary getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Account not found"));
        return UserSummary.from(user);
    }

    // ---- Email verification (NEW) ----

    @Transactional
    public void verifyEmail(String token) {
        User user = authTokenService.consumeToken(token, TokenType.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
    }

    // Same response whether or not the email exists or is already verified,
    // so this endpoint can't be used to find out who has an account.
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email.toLowerCase())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerification);
    }

    // ---- Password reset (NEW) ----

    // Same response whether or not the email exists.
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = authTokenService.createToken(user, TokenType.PASSWORD_RESET);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = authTokenService.consumeToken(token, TokenType.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(newPassword));
        // Clicking a link sent to this inbox also proves they own the email.
        user.setEmailVerified(true);
    }

    private void sendVerification(User user) {
        String token = authTokenService.createToken(user, TokenType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, jwtService.currentExpirationMs(), UserSummary.from(user));
    }
}

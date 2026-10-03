package com.nibbling.server.controller;

import com.nibbling.server.dto.AuthResponse;
import com.nibbling.server.dto.EmailRequest;
import com.nibbling.server.dto.LoginRequest;
import com.nibbling.server.dto.MessageResponse;
import com.nibbling.server.dto.RegisterRequest;
import com.nibbling.server.dto.ResetPasswordRequest;
import com.nibbling.server.dto.TokenRequest;
import com.nibbling.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ---- NEW ----

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody TokenRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(new MessageResponse("Your email is verified"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody EmailRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "If that account needs verifying, a new link is on its way"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "If an account exists for that email, a reset link is on its way"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been updated"));
    }
}

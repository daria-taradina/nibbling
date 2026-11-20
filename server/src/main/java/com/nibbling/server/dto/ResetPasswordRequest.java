package com.nibbling.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Keep the password rules here the same as in RegisterRequest.
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 100, message = "Token is invalid")
        String token,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        String newPassword
) {
}

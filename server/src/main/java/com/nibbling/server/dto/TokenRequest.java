package com.nibbling.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 100, message = "Token is invalid")
        String token
) {
}

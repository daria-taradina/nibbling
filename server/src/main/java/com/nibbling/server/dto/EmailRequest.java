package com.nibbling.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(max = 255)
        String email
) {
}

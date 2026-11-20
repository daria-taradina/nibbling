package com.nibbling.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

// Sends emails through Resend's HTTP API (https://resend.com/docs/api-reference).
// If RESEND_API_KEY isn't set, the link is printed to the console instead,
// so the flow can be tested locally without sending real email.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String fromAddress;
    private final String frontendUrl;

    public EmailService(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from}") String fromAddress,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.restClient = RestClient.create("https://api.resend.com");
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String to, String username, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String html = """
                <p>Hi %s,</p>
                <p>Welcome to Nibbling! Confirm your email to get started:</p>
                <p><a href="%s">Verify my email</a></p>
                <p>This link expires in 24 hours. If you didn't sign up, you can ignore this email.</p>
                """.formatted(HtmlUtils.htmlEscape(username), link);
        send(to, "Verify your Nibbling account", html, link);
    }

    public void sendPasswordResetEmail(String to, String username, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = """
                <p>Hi %s,</p>
                <p>Someone asked to reset your Nibbling password. If that was you, choose a new one here:</p>
                <p><a href="%s">Reset my password</a></p>
                <p>This link expires in 30 minutes. If you didn't ask for this, you can ignore this email.</p>
                """.formatted(HtmlUtils.htmlEscape(username), link);
        send(to, "Reset your Nibbling password", html, link);
    }

    // Never throws: a failed email shouldn't break sign-up, and the
    // forgot-password response must look the same either way.
    private void send(String to, String subject, String html, String link) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY not set. Email to {} not sent. Link: {}", to, link);
            return;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", fromAddress,
                            "to", List.of(to),
                            "subject", subject,
                            "html", html
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}", subject, to, e);
        }
    }
}

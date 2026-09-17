package com.nibbling.server.security.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the signed-in user's profile from GitHub or Discord and adds a few
 * normalized attributes (ATTR_*) so the success handler can treat both
 * providers the same way.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    public static final String ATTR_PROVIDER_ID = "nibbling_provider_id";
    public static final String ATTR_USERNAME = "nibbling_username";
    public static final String ATTR_EMAIL = "nibbling_email";
    public static final String ATTR_EMAIL_VERIFIED = "nibbling_email_verified";

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final RestClient githubApi = RestClient.create("https://api.github.com");

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = new HashMap<>(user.getAttributes());

        switch (provider) {
            case "github" -> {
                attrs.put(ATTR_PROVIDER_ID, String.valueOf(attrs.get("id")));
                attrs.put(ATTR_USERNAME, String.valueOf(attrs.get("login")));
                // GitHub's profile email can be hidden and doesn't say whether it's
                // verified, so ask for the user's primary *verified* email instead.
                String email = fetchGithubVerifiedEmail(request.getAccessToken().getTokenValue());
                if (email != null) {
                    attrs.put(ATTR_EMAIL, email);
                }
                attrs.put(ATTR_EMAIL_VERIFIED, email != null);
            }
            case "discord" -> {
                attrs.put(ATTR_PROVIDER_ID, String.valueOf(attrs.get("id")));
                attrs.put(ATTR_USERNAME, String.valueOf(attrs.get("username")));
                Object email = attrs.get("email");
                if (email instanceof String s && !s.isBlank()) {
                    attrs.put(ATTR_EMAIL, s);
                }
                attrs.put(ATTR_EMAIL_VERIFIED, Boolean.TRUE.equals(attrs.get("verified")));
            }
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"), "Unsupported sign-in provider");
        }

        return new DefaultOAuth2User(user.getAuthorities(), attrs, ATTR_PROVIDER_ID);
    }

    private String fetchGithubVerifiedEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = githubApi.get()
                    .uri("/user/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (emails == null) {
                return null;
            }
            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                    // Primary email first.
                    .sorted(Comparator.comparing(e -> !Boolean.TRUE.equals(e.get("primary"))))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not load GitHub emails", e);
            return null;
        }
    }
}

package com.nibbling.server.security.oauth;

import com.nibbling.server.entity.User;
import com.nibbling.server.security.JwtService;
import com.nibbling.server.service.OAuthAccountService;
import com.nibbling.server.service.OAuthAccountService.OAuthEmailRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * After GitHub/Discord says "this person is who they claim", find or create
 * the Nibbling account, issue our own JWT, and send the browser back to the
 * frontend. The token goes in the URL *fragment* (#token=...), which browsers
 * never send to servers, so it doesn't end up in any server logs.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final OAuthAccountService oAuthAccountService;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            OAuthAccountService oAuthAccountService,
            JwtService jwtService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.oAuthAccountService = oAuthAccountService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target;
        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User principal = token.getPrincipal();

            User user = oAuthAccountService.findOrCreateUser(
                    token.getAuthorizedClientRegistrationId(),
                    principal.getAttribute(CustomOAuth2UserService.ATTR_PROVIDER_ID),
                    principal.getAttribute(CustomOAuth2UserService.ATTR_EMAIL),
                    Boolean.TRUE.equals(principal.getAttribute(CustomOAuth2UserService.ATTR_EMAIL_VERIFIED)),
                    principal.getAttribute(CustomOAuth2UserService.ATTR_USERNAME)
            );

            String jwt = jwtService.generateToken(user.getId(), user.getEmail());
            target = frontendUrl + "/oauth/callback#token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        } catch (OAuthEmailRequiredException e) {
            target = frontendUrl + "/login?error=oauth_email";
        } catch (Exception e) {
            log.error("OAuth sign-in failed after provider approval", e);
            target = frontendUrl + "/login?error=oauth";
        }

        // The short-lived OAuth session is no longer needed; the JWT takes over.
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        response.sendRedirect(target);
    }
}

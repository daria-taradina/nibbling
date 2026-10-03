package com.nibbling.server.config;

import com.nibbling.server.security.JwtAuthenticationFilter;
import com.nibbling.server.security.RateLimitFilter;
import com.nibbling.server.security.oauth.CustomOAuth2UserService;
import com.nibbling.server.security.oauth.OAuth2LoginFailureHandler;
import com.nibbling.server.security.oauth.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Only these auth endpoints are public. Anything new under /api/auth
    // (for example a future /api/auth/me) requires a valid JWT by default.
    private static final String[] PUBLIC_AUTH_POSTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2FailureHandler;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomOAuth2UserService oAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2SuccessHandler,
            OAuth2LoginFailureHandler oAuth2FailureHandler,
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless JWT-in-header API, no auth cookies -> no CSRF exposure.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            // No login sessions. (The OAuth redirect briefly uses a session to
            // remember its "state" value; the success handler ends it.)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_POSTS).permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            // GitHub + Discord sign-in. Starts at /oauth2/authorization/{provider}.
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            // API callers without a valid token get a plain 401,
            // not a redirect to a login page.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // Hardening headers. This server only returns JSON, so it never
            // needs to load scripts or be shown inside a frame.
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            // Rate limiting runs first so floods are rejected cheaply.
            .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Set CORS_ALLOWED_ORIGINS to add the deployed frontend (e.g. the Vercel URL).
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Retry-After"));
        // The JWT travels in the Authorization header, not a cookie.
        // Flip this to true only if the app moves to httpOnly cookies.
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

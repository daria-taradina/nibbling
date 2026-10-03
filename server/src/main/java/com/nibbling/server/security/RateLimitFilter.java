package com.nibbling.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple per-IP rate limiter (fixed time windows, kept in memory).
 *
 * Sensitive auth endpoints get tight limits to slow down password guessing
 * and email spam; everything else shares a looser general limit.
 *
 * Limitation: counts live in this one server's memory, so they reset on
 * restart and aren't shared between instances. That's fine for a single
 * Render instance; a multi-instance deploy would need a shared store (Redis).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(int limit, Duration window) {
    }

    private static final Map<String, Rule> AUTH_RULES = Map.of(
            "/api/auth/login", new Rule(10, Duration.ofMinutes(1)),
            "/api/auth/register", new Rule(5, Duration.ofMinutes(10)),
            "/api/auth/forgot-password", new Rule(3, Duration.ofMinutes(15)),
            "/api/auth/resend-verification", new Rule(3, Duration.ofMinutes(15)),
            "/api/auth/reset-password", new Rule(10, Duration.ofMinutes(15)),
            "/api/auth/verify-email", new Rule(10, Duration.ofMinutes(15))
    );
    private static final Rule GENERAL_RULE = new Rule(120, Duration.ofMinutes(1));

    private static final long LONGEST_WINDOW_MS = Duration.ofMinutes(15).toMillis();
    private static final long CLEANUP_EVERY_MS = Duration.ofMinutes(5).toMillis();

    private static final class Window {
        final long startMs;
        final AtomicInteger count = new AtomicInteger();

        Window(long startMs) {
            this.startMs = startMs;
        }
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupMs = new AtomicLong(System.currentTimeMillis());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight requests don't count.
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Rule authRule = AUTH_RULES.get(path);
        Rule rule = authRule != null ? authRule : GENERAL_RULE;
        long windowMs = rule.window().toMillis();

        // With server.forward-headers-strategy=framework, getRemoteAddr()
        // is the real client IP even behind Render's proxy.
        String key = request.getRemoteAddr() + "|" + (authRule != null ? path : "*");
        long now = System.currentTimeMillis();

        Window window = windows.compute(key, (k, current) ->
                (current == null || now - current.startMs >= windowMs) ? new Window(now) : current);
        int count = window.count.incrementAndGet();

        cleanUpOldWindows(now);

        if (count > rule.limit()) {
            long retryAfterSeconds = Math.max(1, (window.startMs + windowMs - now + 999) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"timestamp":"%s","status":429,"error":"Too Many Requests","message":"Too many attempts. Try again in %d seconds."}"""
                    .formatted(Instant.now(), retryAfterSeconds));
            return;
        }

        chain.doFilter(request, response);
    }

    private void cleanUpOldWindows(long now) {
        long last = lastCleanupMs.get();
        if (now - last < CLEANUP_EVERY_MS || !lastCleanupMs.compareAndSet(last, now)) {
            return;
        }
        windows.entrySet().removeIf(e -> now - e.getValue().startMs >= LONGEST_WINDOW_MS);
    }
}

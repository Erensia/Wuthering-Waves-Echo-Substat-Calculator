package com.wuwa.echograder.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final Policy LOGIN_POLICY = new Policy("login", 10, 60);
    private static final Policy SIGNUP_POLICY = new Policy("signup", 5, 3600);
    private static final Policy USER_SEARCH_POLICY = new Policy("user-search", 60, 60);

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private final Clock clock;

    public RequestRateLimitFilter() {
        this(Clock.systemUTC());
    }

    RequestRateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Policy policy = policyFor(request);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = clock.instant().getEpochSecond();
        String key = request.getRemoteAddr() + ":" + policy.name();
        LimitResult result = counters
                .computeIfAbsent(key, ignored -> new WindowCounter(now))
                .increment(now, policy);

        if ((requestCount.incrementAndGet() & 255) == 0) {
            counters.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        }

        if (!result.allowed()) {
            response.setStatus(429);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(result.retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"title\":\"Too Many Requests\",\"status\":429,"
                            + "\"detail\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Policy policyFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && "/api/v1/auth/login".equals(path)) {
            return LOGIN_POLICY;
        }
        if ("POST".equals(method) && "/api/v1/auth/signup".equals(path)) {
            return SIGNUP_POLICY;
        }
        if ("GET".equals(method) && "/api/v1/users/search".equals(path)) {
            return USER_SEARCH_POLICY;
        }
        return null;
    }

    private record Policy(String name, int limit, long windowSeconds) {
    }

    private record LimitResult(boolean allowed, long retryAfterSeconds) {
    }

    private static final class WindowCounter {

        private long windowStartedAt;
        private int count;

        private WindowCounter(long windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }

        private synchronized LimitResult increment(long now, Policy policy) {
            if (now - windowStartedAt >= policy.windowSeconds()) {
                windowStartedAt = now;
                count = 0;
            }

            count++;
            long retryAfter = Math.max(1, policy.windowSeconds() - (now - windowStartedAt));
            return new LimitResult(count <= policy.limit(), retryAfter);
        }

        private synchronized boolean isExpired(long now) {
            return now - windowStartedAt >= SIGNUP_POLICY.windowSeconds();
        }
    }
}

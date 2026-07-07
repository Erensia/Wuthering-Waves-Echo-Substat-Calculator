package com.wuwa.echograder.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestRateLimitFilterTest {

    private final RequestRateLimitFilter filter = new RequestRateLimitFilter(
            Clock.fixed(Instant.parse("2026-07-02T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void limitsLoginAttemptsByClientAddress() throws Exception {
        for (int attempt = 1; attempt <= 10; attempt++) {
            assertThat(executeLogin("192.0.2.10").getStatus()).isEqualTo(200);
        }

        MockHttpServletResponse blocked = executeLogin("192.0.2.10");

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("요청이 너무 많습니다");
    }

    @Test
    void keepsLimitsIndependentForDifferentClientAddresses() throws Exception {
        for (int attempt = 1; attempt <= 10; attempt++) {
            executeLogin("192.0.2.10");
        }

        assertThat(executeLogin("192.0.2.20").getStatus()).isEqualTo(200);
    }

    @Test
    void usesForwardedClientAddressBehindTrustedProxy() throws Exception {
        for (int attempt = 1; attempt <= 10; attempt++) {
            executeLogin("10.0.0.2", "198.51.100.10");
        }

        assertThat(executeLogin("10.0.0.2", "198.51.100.10").getStatus()).isEqualTo(429);
        assertThat(executeLogin("10.0.0.2", "198.51.100.20").getStatus()).isEqualTo(200);
    }

    private MockHttpServletResponse executeLogin(String remoteAddress) throws Exception {
        return executeLogin(remoteAddress, null);
    }

    private MockHttpServletResponse executeLogin(String remoteAddress, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}

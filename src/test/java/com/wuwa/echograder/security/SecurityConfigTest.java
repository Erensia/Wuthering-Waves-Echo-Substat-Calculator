package com.wuwa.echograder.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.auth.AuthResult;
import com.wuwa.echograder.auth.AuthService;
import com.wuwa.echograder.auth.UserAccount;
import com.wuwa.echograder.loadout.LoadoutService;
import com.wuwa.echograder.score.ScoreService;
import com.wuwa.echograder.web.AuthController;
import com.wuwa.echograder.web.CsrfController;
import com.wuwa.echograder.web.LoadoutController;
import com.wuwa.echograder.web.ScoreController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        AuthController.class,
        CsrfController.class,
        LoadoutController.class,
        ScoreController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private LoadoutService loadoutService;

    @MockitoBean
    private ScoreService scoreService;

    @Test
    void issuesCsrfTokenAndSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("default-src 'self'")));
    }

    @Test
    void rejectsStateChangingRequestWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/scores/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validScoreRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsProtectedApiWithoutSignedInSession() throws Exception {
        mockMvc.perform(get("/api/v1/loadouts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsProtectedApiWithSignedInSession() throws Exception {
        UUID userId = UUID.randomUUID();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.USER_ID_SESSION_KEY, userId);
        UserAccount user = new UserAccount("tester", "hashed-password");
        when(authService.requireUser(any())).thenReturn(user);
        when(loadoutService.findAll(user)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/loadouts").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void rotatesSessionIdAfterLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String previousSessionId = session.getId();
        when(authService.login(any(), any()))
                .thenReturn(new AuthResult(UUID.randomUUID(), "tester"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());

        assertThat(session.getId()).isNotEqualTo(previousSessionId);
    }

    private String validScoreRequest() {
        return """
                {
                  "firstEchoMainStat": "CRIT_RATE",
                  "echoes": [
                    {"cost": "COST_4", "critRate": 8.7, "critDamage": 17.4},
                    {"cost": "COST_3", "critRate": 9.3, "critDamage": 18.6},
                    {"cost": "COST_3", "critRate": 8.1, "critDamage": 15.0},
                    {"cost": "COST_1", "critRate": 7.5, "critDamage": 14.2},
                    {"cost": "COST_1", "critRate": 8.5, "critDamage": 16.2}
                  ]
                }
                """;
    }
}

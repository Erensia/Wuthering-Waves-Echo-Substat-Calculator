package com.wuwa.echograder.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository repository;

    @Test
    void signupStoresPlainPasswordAndSignsUserIn() {
        AuthService service = new AuthService(repository);
        MockHttpSession session = new MockHttpSession();
        UserAccount saved = new UserAccount("tester", "plain-password");
        saved.onCreate();

        when(repository.existsByUsername("tester")).thenReturn(false);
        when(repository.saveAndFlush(org.mockito.ArgumentMatchers.any(UserAccount.class))).thenReturn(saved);

        AuthResult result = service.signup(new AuthRequest("Tester", "plain-password"), session);

        assertThat(result.username()).isEqualTo("tester");
        assertThat(session.getAttribute(AuthService.USER_ID_SESSION_KEY)).isEqualTo(saved.getId());
        verify(repository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(user ->
                user.getUsername().equals("tester")
                        && user.getPassword().equals("plain-password")));
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthService service = new AuthService(repository);
        UserAccount user = new UserAccount("tester", "correct-password");
        user.onCreate();
        when(repository.findByUsername("tester")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.login(new AuthRequest("tester", "wrong-password"), new MockHttpSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }
}

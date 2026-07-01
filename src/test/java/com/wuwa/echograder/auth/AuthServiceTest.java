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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository repository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @Test
    void signupStoresHashedPasswordAndSignsUserIn() {
        AuthService service = new AuthService(repository, passwordEncoder);
        MockHttpSession session = new MockHttpSession();

        when(repository.existsByUsername("tester")).thenReturn(false);
        when(repository.saveAndFlush(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> {
                    UserAccount user = invocation.getArgument(0);
                    user.onCreate();
                    return user;
                });

        AuthResult result = service.signup(new AuthRequest("Tester", "plain-password"), session);

        assertThat(result.username()).isEqualTo("tester");
        assertThat(session.getAttribute(AuthService.USER_ID_SESSION_KEY)).isNotNull();
        verify(repository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(user ->
                user.getUsername().equals("tester")
                        && !user.getPasswordHash().equals("plain-password")
                        && passwordEncoder.matches("plain-password", user.getPasswordHash())));
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthService service = new AuthService(repository, passwordEncoder);
        UserAccount user = new UserAccount("tester", passwordEncoder.encode("correct-password"));
        user.onCreate();
        when(repository.findByUsername("tester")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.login(new AuthRequest("tester", "wrong-password"), new MockHttpSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }

    @Test
    void loginAcceptsCorrectPassword() {
        AuthService service = new AuthService(repository, passwordEncoder);
        UserAccount user = new UserAccount("tester", passwordEncoder.encode("correct-password"));
        user.onCreate();
        when(repository.findByUsername("tester")).thenReturn(Optional.of(user));
        MockHttpSession session = new MockHttpSession();

        AuthResult result = service.login(
                new AuthRequest("tester", "correct-password"),
                session);

        assertThat(result.username()).isEqualTo("tester");
        assertThat(session.getAttribute(AuthService.USER_ID_SESSION_KEY)).isEqualTo(user.getId());
    }

    @Test
    void changePasswordUpdatesSignedInUsersPassword() {
        AuthService service = new AuthService(repository, passwordEncoder);
        MockHttpSession session = new MockHttpSession();
        UserAccount user = new UserAccount("tester", passwordEncoder.encode("old-password"));
        user.onCreate();
        session.setAttribute(AuthService.USER_ID_SESSION_KEY, user.getId());
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        service.changePassword(
                new PasswordChangeRequest("old-password", "new-password"),
                session);

        assertThat(user.getPasswordHash()).isNotEqualTo("new-password");
        assertThat(passwordEncoder.matches("new-password", user.getPasswordHash())).isTrue();
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        AuthService service = new AuthService(repository, passwordEncoder);
        MockHttpSession session = new MockHttpSession();
        String originalPasswordHash = passwordEncoder.encode("correct-password");
        UserAccount user = new UserAccount("tester", originalPasswordHash);
        user.onCreate();
        session.setAttribute(AuthService.USER_ID_SESSION_KEY, user.getId());
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(
                new PasswordChangeRequest("wrong-password", "new-password"),
                session))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
        assertThat(user.getPasswordHash()).isEqualTo(originalPasswordHash);
    }
}

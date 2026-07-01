package com.wuwa.echograder.auth;

import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    public static final String USER_ID_SESSION_KEY = "userId";

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResult signup(AuthRequest request, HttpSession session) {
        String username = normalizeUsername(request.username());
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        try {
            String passwordHash = passwordEncoder.encode(request.password());
            UserAccount user = repository.saveAndFlush(new UserAccount(username, passwordHash));
            signIn(session, user);
            return AuthResult.from(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.", exception);
        }
    }

    @Transactional(readOnly = true)
    public AuthResult login(AuthRequest request, HttpSession session) {
        UserAccount user = repository.findByUsername(normalizeUsername(request.username()))
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "아이디 또는 비밀번호가 올바르지 않습니다."));
        signIn(session, user);
        return AuthResult.from(user);
    }

    @Transactional(readOnly = true)
    public UserAccount requireUser(HttpSession session) {
        Object value = session.getAttribute(USER_ID_SESSION_KEY);
        if (!(value instanceof UUID userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return repository.findById(userId)
                .orElseThrow(() -> {
                    session.invalidate();
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
                });
    }

    @Transactional(readOnly = true)
    public AuthResult currentUser(HttpSession session) {
        return AuthResult.from(requireUser(session));
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request, HttpSession session) {
        UserAccount user = requireUser(session);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private void signIn(HttpSession session, UserAccount user) {
        session.setAttribute(USER_ID_SESSION_KEY, user.getId());
    }

    private String normalizeUsername(String username) {
        return username.strip().toLowerCase(Locale.ROOT);
    }
}

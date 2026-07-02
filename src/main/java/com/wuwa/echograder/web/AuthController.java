package com.wuwa.echograder.web;

import com.wuwa.echograder.auth.AuthRequest;
import com.wuwa.echograder.auth.AuthResult;
import com.wuwa.echograder.auth.AuthService;
import com.wuwa.echograder.auth.PasswordChangeRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResult signup(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {
        AuthResult result = authService.signup(request, session);
        httpRequest.changeSessionId();
        return result;
    }

    @PostMapping("/login")
    public AuthResult login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {
        AuthResult result = authService.login(request, session);
        httpRequest.changeSessionId();
        return result;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        authService.logout(session);
    }

    @GetMapping("/me")
    public AuthResult me(HttpSession session) {
        return authService.currentUser(session);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpSession session) {
        authService.changePassword(request, session);
    }
}

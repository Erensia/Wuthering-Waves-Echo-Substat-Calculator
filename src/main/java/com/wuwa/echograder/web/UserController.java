package com.wuwa.echograder.web;

import java.util.List;

import com.wuwa.echograder.auth.AuthService;
import com.wuwa.echograder.auth.UserSearchResult;
import com.wuwa.echograder.auth.UserSearchService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserSearchService userSearchService;
    private final AuthService authService;

    public UserController(UserSearchService userSearchService, AuthService authService) {
        this.userSearchService = userSearchService;
        this.authService = authService;
    }

    @GetMapping("/search")
    public List<UserSearchResult> search(@RequestParam String query, HttpSession session) {
        authService.requireUser(session);
        return userSearchService.search(query);
    }
}

package com.wuwa.echograder.web;

import java.util.List;

import com.wuwa.echograder.loadout.CharacterScoreResult;
import com.wuwa.echograder.auth.AuthService;
import com.wuwa.echograder.loadout.LoadoutService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final LoadoutService loadoutService;
    private final AuthService authService;

    public DashboardController(LoadoutService loadoutService, AuthService authService) {
        this.loadoutService = loadoutService;
        this.authService = authService;
    }

    @GetMapping("/characters")
    public List<CharacterScoreResult> findCharacterScores(HttpSession session) {
        return loadoutService.findCharacterScores(authService.requireUser(session));
    }
}

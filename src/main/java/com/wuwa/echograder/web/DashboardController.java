package com.wuwa.echograder.web;

import java.util.List;

import com.wuwa.echograder.loadout.CharacterScoreResult;
import com.wuwa.echograder.loadout.LoadoutService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final LoadoutService loadoutService;

    public DashboardController(LoadoutService loadoutService) {
        this.loadoutService = loadoutService;
    }

    @GetMapping("/characters")
    public List<CharacterScoreResult> findCharacterScores() {
        return loadoutService.findCharacterScores();
    }
}

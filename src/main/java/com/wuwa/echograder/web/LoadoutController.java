package com.wuwa.echograder.web;

import com.wuwa.echograder.loadout.LoadoutService;
import com.wuwa.echograder.loadout.SaveLoadoutRequest;
import com.wuwa.echograder.loadout.SavedLoadoutResult;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loadouts")
public class LoadoutController {

    private final LoadoutService loadoutService;

    public LoadoutController(LoadoutService loadoutService) {
        this.loadoutService = loadoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedLoadoutResult save(@Valid @RequestBody SaveLoadoutRequest request) {
        return loadoutService.save(request);
    }
}

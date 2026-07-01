package com.wuwa.echograder.web;

import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.auth.AuthService;
import com.wuwa.echograder.loadout.LoadoutService;
import com.wuwa.echograder.loadout.LoadoutResult;
import com.wuwa.echograder.loadout.SaveLoadoutRequest;
import com.wuwa.echograder.loadout.SavedLoadoutResult;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loadouts")
public class LoadoutController {

    private final LoadoutService loadoutService;
    private final AuthService authService;

    public LoadoutController(LoadoutService loadoutService, AuthService authService) {
        this.loadoutService = loadoutService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedLoadoutResult save(
            @Valid @RequestBody SaveLoadoutRequest request,
            HttpSession session) {
        return loadoutService.save(request, authService.requireUser(session));
    }

    @GetMapping
    public List<LoadoutResult> findAll(HttpSession session) {
        return loadoutService.findAll(authService.requireUser(session));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, HttpSession session) {
        if (!loadoutService.delete(id, authService.requireUser(session))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장한 프리셋을 찾을 수 없습니다.");
        }
    }
}

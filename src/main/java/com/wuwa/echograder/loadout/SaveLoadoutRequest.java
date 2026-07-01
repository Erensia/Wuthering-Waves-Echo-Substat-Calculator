package com.wuwa.echograder.loadout;

import com.wuwa.echograder.score.ScoreRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveLoadoutRequest(
        @Size(max = 100) String name,
        @NotNull @Valid ScoreRequest scoreRequest) {
}

package com.wuwa.echograder.loadout;

import java.time.Instant;
import java.util.UUID;

import com.wuwa.echograder.score.ScoreResult;

public record SavedLoadoutResult(
        UUID id,
        String name,
        Instant createdAt,
        ScoreResult result) {
}

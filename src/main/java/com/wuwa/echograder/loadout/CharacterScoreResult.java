package com.wuwa.echograder.loadout;

import java.math.BigDecimal;

import com.wuwa.echograder.score.Grade;

public record CharacterScoreResult(
        String characterName,
        BigDecimal bestScore,
        Grade grade,
        String gradeLabel,
        long loadoutCount) {

    static CharacterScoreResult from(CharacterScoreSummary summary) {
        Grade grade = Grade.from(summary.getBestScore());
        return new CharacterScoreResult(
                summary.getCharacterName(),
                summary.getBestScore(),
                grade,
                grade.getLabel(),
                summary.getLoadoutCount());
    }
}

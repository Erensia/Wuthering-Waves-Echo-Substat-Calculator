package com.wuwa.echograder.score;

import java.math.BigDecimal;
import java.util.List;

public record ScoreResult(
        BigDecimal totalCritRate,
        BigDecimal totalCritDamage,
        BigDecimal mainStatScore,
        BigDecimal score,
        Grade grade,
        String gradeLabel,
        BigDecimal pointsToNextGrade,
        List<EchoScoreResult> echoScores) {
}

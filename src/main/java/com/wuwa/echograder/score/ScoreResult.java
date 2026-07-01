package com.wuwa.echograder.score;

import java.math.BigDecimal;

public record ScoreResult(
        BigDecimal totalCritRate,
        BigDecimal totalCritDamage,
        BigDecimal mainStatScore,
        BigDecimal score,
        Grade grade,
        String gradeLabel,
        BigDecimal pointsToNextGrade) {
}

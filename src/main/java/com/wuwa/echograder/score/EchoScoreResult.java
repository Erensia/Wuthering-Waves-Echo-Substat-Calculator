package com.wuwa.echograder.score;

import java.math.BigDecimal;

public record EchoScoreResult(
        int slotNumber,
        EchoCost cost,
        int costValue,
        BigDecimal score,
        EchoGrade grade,
        String gradeLabel) {
}

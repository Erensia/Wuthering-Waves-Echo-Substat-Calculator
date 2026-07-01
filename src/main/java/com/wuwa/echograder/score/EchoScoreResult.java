package com.wuwa.echograder.score;

import java.math.BigDecimal;

public record EchoScoreResult(
        int slotNumber,
        BigDecimal score,
        EchoGrade grade,
        String gradeLabel) {
}

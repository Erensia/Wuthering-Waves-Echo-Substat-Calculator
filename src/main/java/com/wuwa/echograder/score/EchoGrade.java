package com.wuwa.echograder.score;

import java.math.BigDecimal;

public enum EchoGrade {
    EXTREME("극종결"),
    COMPLETE("종결"),
    NEAR_COMPLETE("준종결"),
    NEED_REBUILD("다시 파밍 필요");

    private static final BigDecimal EXTREME_THRESHOLD = new BigDecimal("37.8");
    private static final BigDecimal COMPLETE_THRESHOLD = new BigDecimal("33.6");
    private static final BigDecimal NEAR_COMPLETE_THRESHOLD = new BigDecimal("29.4");

    private final String label;

    EchoGrade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static EchoGrade from(BigDecimal score) {
        if (score.compareTo(EXTREME_THRESHOLD) >= 0) {
            return EXTREME;
        }
        if (score.compareTo(COMPLETE_THRESHOLD) >= 0) {
            return COMPLETE;
        }
        if (score.compareTo(NEAR_COMPLETE_THRESHOLD) >= 0) {
            return NEAR_COMPLETE;
        }
        return NEED_REBUILD;
    }
}

package com.wuwa.echograder.score;

import java.math.BigDecimal;

public enum Grade {
    EXTREME("극종결"),
    COMPLETE("종결"),
    NEAR_COMPLETE("준종결"),
    NEED_REBUILD("다시 맞출 필요 있음");

    private final String label;

    Grade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Grade from(BigDecimal score) {
        if (score.compareTo(new BigDecimal("230")) >= 0) {
            return EXTREME;
        }
        if (score.compareTo(new BigDecimal("200")) >= 0) {
            return COMPLETE;
        }
        if (score.compareTo(new BigDecimal("170")) >= 0) {
            return NEAR_COMPLETE;
        }
        return NEED_REBUILD;
    }
}

package com.wuwa.echograder.score;

import java.math.BigDecimal;

public enum MainStat {
    CRIT_RATE(new BigDecimal("22.0"), new BigDecimal("44.0")),
    CRIT_DAMAGE(new BigDecimal("44.0"), new BigDecimal("44.0"));

    private final BigDecimal displayedValue;
    private final BigDecimal score;

    MainStat(BigDecimal displayedValue, BigDecimal score) {
        this.displayedValue = displayedValue;
        this.score = score;
    }

    public BigDecimal getDisplayedValue() {
        return displayedValue;
    }

    public BigDecimal getScore() {
        return score;
    }
}

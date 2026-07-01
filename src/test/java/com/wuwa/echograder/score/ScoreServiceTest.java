package com.wuwa.echograder.score;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class ScoreServiceTest {

    private final ScoreService scoreService = new ScoreService();

    @Test
    void calculatesScoreIncludingFirstEchoMainStat() {
        ScoreRequest request = requestWithTotals("42.1", "81.4", MainStat.CRIT_RATE);

        ScoreResult result = scoreService.calculate(request);

        assertThat(result.score()).isEqualByComparingTo("209.6");
        assertThat(result.grade()).isEqualTo(Grade.COMPLETE);
        assertThat(result.pointsToNextGrade()).isEqualByComparingTo("20.4");
    }

    @Test
    void appliesGradeBoundaries() {
        assertThat(scoreService.calculate(requestWithTotals("63", "60", MainStat.CRIT_DAMAGE)).grade())
                .isEqualTo(Grade.EXTREME);
        assertThat(scoreService.calculate(requestWithTotals("48", "60", MainStat.CRIT_DAMAGE)).grade())
                .isEqualTo(Grade.COMPLETE);
        assertThat(scoreService.calculate(requestWithTotals("33", "60", MainStat.CRIT_DAMAGE)).grade())
                .isEqualTo(Grade.NEAR_COMPLETE);
        assertThat(scoreService.calculate(requestWithTotals("32.9", "60", MainStat.CRIT_DAMAGE)).grade())
                .isEqualTo(Grade.NEED_REBUILD);
    }

    private ScoreRequest requestWithTotals(String critRate, String critDamage, MainStat mainStat) {
        return new ScoreRequest(mainStat, List.of(
                new EchoInput(new BigDecimal(critRate), new BigDecimal(critDamage)),
                new EchoInput(BigDecimal.ZERO, BigDecimal.ZERO),
                new EchoInput(BigDecimal.ZERO, BigDecimal.ZERO),
                new EchoInput(BigDecimal.ZERO, BigDecimal.ZERO),
                new EchoInput(BigDecimal.ZERO, BigDecimal.ZERO)));
    }
}

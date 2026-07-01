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

    @Test
    void calculatesAndGradesEachEchoUsingEqualIntervals() {
        ScoreRequest request = new ScoreRequest(MainStat.CRIT_RATE, List.of(
                echo("10.5", "21.0"),
                echo("8.4", "21.0"),
                echo("6.3", "21.0"),
                echo("4.2", "21.0"),
                echo("6.3", "12.6")));

        List<EchoScoreResult> echoes = scoreService.calculate(request).echoScores();

        assertThat(echoes).extracting(EchoScoreResult::score)
                .containsExactly(
                        new BigDecimal("42.0"),
                        new BigDecimal("37.8"),
                        new BigDecimal("33.6"),
                        new BigDecimal("29.4"),
                        new BigDecimal("25.2"));
        assertThat(echoes).extracting(EchoScoreResult::grade)
                .containsExactly(
                        EchoGrade.EXTREME,
                        EchoGrade.EXTREME,
                        EchoGrade.COMPLETE,
                        EchoGrade.NEAR_COMPLETE,
                        EchoGrade.NEED_REBUILD);
    }

    @Test
    void appliesIndividualEchoGradeBoundaries() {
        ScoreRequest request = new ScoreRequest(MainStat.CRIT_DAMAGE, List.of(
                echo("0", "37.7"),
                echo("0", "33.5"),
                echo("0", "29.3"),
                echo("0", "37.8"),
                echo("0", "33.6")));

        assertThat(scoreService.calculate(request).echoScores())
                .extracting(EchoScoreResult::grade)
                .containsExactly(
                        EchoGrade.COMPLETE,
                        EchoGrade.NEAR_COMPLETE,
                        EchoGrade.NEED_REBUILD,
                        EchoGrade.EXTREME,
                        EchoGrade.COMPLETE);
    }

    private EchoInput echo(String critRate, String critDamage) {
        return new EchoInput(new BigDecimal(critRate), new BigDecimal(critDamage));
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

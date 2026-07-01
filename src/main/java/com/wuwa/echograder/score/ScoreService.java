package com.wuwa.echograder.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    private static final BigDecimal TWO = new BigDecimal("2");

    public ScoreResult calculate(ScoreRequest request) {
        BigDecimal totalCritRate = request.echoes().stream()
                .map(EchoInput::critRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCritDamage = request.echoes().stream()
                .map(EchoInput::critDamage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mainStatScore = request.firstEchoMainStat().getScore();
        BigDecimal score = totalCritRate.multiply(TWO)
                .add(totalCritDamage)
                .add(mainStatScore)
                .setScale(1, RoundingMode.HALF_UP);
        Grade grade = Grade.from(score);
        List<EchoScoreResult> echoScores = IntStream.range(0, request.echoes().size())
                .mapToObj(index -> calculateEchoScore(index + 1, request.echoes().get(index)))
                .toList();

        return new ScoreResult(
                totalCritRate.setScale(1, RoundingMode.HALF_UP),
                totalCritDamage.setScale(1, RoundingMode.HALF_UP),
                mainStatScore,
                score,
                grade,
                grade.getLabel(),
                pointsToNextGrade(score),
                echoScores);
    }

    private EchoScoreResult calculateEchoScore(int slotNumber, EchoInput echo) {
        BigDecimal score = echo.critRate().multiply(TWO)
                .add(echo.critDamage())
                .setScale(1, RoundingMode.HALF_UP);
        EchoGrade grade = EchoGrade.from(score);
        return new EchoScoreResult(slotNumber, score, grade, grade.getLabel());
    }

    private BigDecimal pointsToNextGrade(BigDecimal score) {
        BigDecimal target = switch (Grade.from(score)) {
            case NEED_REBUILD -> new BigDecimal("170");
            case NEAR_COMPLETE -> new BigDecimal("200");
            case COMPLETE -> new BigDecimal("230");
            case EXTREME -> score;
        };
        return target.subtract(score).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP);
    }
}

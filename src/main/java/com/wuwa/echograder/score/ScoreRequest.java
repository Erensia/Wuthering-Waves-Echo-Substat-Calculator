package com.wuwa.echograder.score;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScoreRequest(
        @NotNull MainStat firstEchoMainStat,
        @NotNull @Size(min = 5, max = 5) List<@Valid EchoInput> echoes) {

    private static final int MAX_TOTAL_COST = 12;

    @AssertTrue(message = "1번 에코는 4코스트여야 합니다.")
    public boolean isFirstEchoFourCost() {
        return echoes == null
                || echoes.isEmpty()
                || echoes.getFirst() == null
                || echoes.getFirst().cost() == null
                || echoes.getFirst().cost() == EchoCost.COST_4;
    }

    @AssertTrue(message = "에코 코스트 합계는 12 이하여야 합니다.")
    public boolean isTotalCostWithinLimit() {
        return echoes == null
                || echoes.stream().anyMatch(echo -> echo == null || echo.cost() == null)
                || echoes.stream().mapToInt(echo -> echo.cost().getValue()).sum() <= MAX_TOTAL_COST;
    }
}

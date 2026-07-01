package com.wuwa.echograder.score;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScoreRequest(
        @NotNull MainStat firstEchoMainStat,
        @NotNull @Size(min = 5, max = 5) List<@Valid EchoInput> echoes) {

    @AssertTrue(message = "1번 에코는 4코스트여야 합니다.")
    public boolean isFirstEchoFourCost() {
        return echoes == null
                || echoes.isEmpty()
                || echoes.getFirst() == null
                || echoes.getFirst().cost() == null
                || echoes.getFirst().cost() == EchoCost.COST_4;
    }
}

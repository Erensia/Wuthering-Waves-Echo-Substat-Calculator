package com.wuwa.echograder.score;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScoreRequest(
        @NotNull MainStat firstEchoMainStat,
        @NotNull @Size(min = 5, max = 5) List<@Valid EchoInput> echoes) {
}

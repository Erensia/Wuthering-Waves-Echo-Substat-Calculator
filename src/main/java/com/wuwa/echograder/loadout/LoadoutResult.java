package com.wuwa.echograder.loadout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.score.Grade;
import com.wuwa.echograder.score.MainStat;

public record LoadoutResult(
        UUID id,
        String name,
        MainStat firstEchoMainStat,
        BigDecimal score,
        Grade grade,
        String gradeLabel,
        Instant createdAt,
        List<EchoStatResult> echoes) {

    public static LoadoutResult from(Loadout loadout) {
        List<EchoStatResult> echoes = loadout.getEchoes().stream()
                .sorted(Comparator.comparingInt(EchoStat::getSlotNumber))
                .map(EchoStatResult::from)
                .toList();
        return new LoadoutResult(
                loadout.getId(),
                loadout.getName(),
                loadout.getFirstEchoMainStat(),
                loadout.getScore(),
                loadout.getGrade(),
                loadout.getGrade().getLabel(),
                loadout.getCreatedAt(),
                echoes);
    }
}

package com.wuwa.echograder.loadout;

import java.math.BigDecimal;

public record EchoStatResult(
        short slotNumber,
        short cost,
        BigDecimal critRate,
        BigDecimal critDamage) {

    public static EchoStatResult from(EchoStat echo) {
        return new EchoStatResult(
                echo.getSlotNumber(),
                echo.getCost(),
                echo.getCritRate(),
                echo.getCritDamage());
    }
}

package com.wuwa.echograder.loadout;

import java.math.BigDecimal;

public interface CharacterScoreSummary {

    String getCharacterName();

    BigDecimal getBestScore();

    long getLoadoutCount();
}

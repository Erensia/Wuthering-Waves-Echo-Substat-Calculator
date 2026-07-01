package com.wuwa.echograder.score;

public enum EchoCost {
    COST_4(4),
    COST_3(3),
    COST_1(1);

    private final int value;

    EchoCost(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

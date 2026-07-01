package com.wuwa.echograder.loadout;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "echo_stat")
public class EchoStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loadout_id", nullable = false)
    private Loadout loadout;

    @Column(name = "slot_number", nullable = false)
    private short slotNumber;

    @Column(nullable = false)
    private short cost;

    @Column(name = "crit_rate", nullable = false, precision = 4, scale = 1)
    private BigDecimal critRate;

    @Column(name = "crit_damage", nullable = false, precision = 4, scale = 1)
    private BigDecimal critDamage;

    protected EchoStat() {
    }

    public EchoStat(int slotNumber, int cost, BigDecimal critRate, BigDecimal critDamage) {
        this.slotNumber = (short) slotNumber;
        this.cost = (short) cost;
        this.critRate = critRate;
        this.critDamage = critDamage;
    }

    void assignTo(Loadout loadout) {
        this.loadout = loadout;
    }
}

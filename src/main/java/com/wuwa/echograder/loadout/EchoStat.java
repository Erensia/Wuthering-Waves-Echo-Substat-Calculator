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
    private int slotNumber;

    @Column(name = "crit_rate", nullable = false, precision = 4, scale = 1)
    private BigDecimal critRate;

    @Column(name = "crit_damage", nullable = false, precision = 4, scale = 1)
    private BigDecimal critDamage;

    protected EchoStat() {
    }

    public EchoStat(int slotNumber, BigDecimal critRate, BigDecimal critDamage) {
        this.slotNumber = slotNumber;
        this.critRate = critRate;
        this.critDamage = critDamage;
    }

    void assignTo(Loadout loadout) {
        this.loadout = loadout;
    }
}

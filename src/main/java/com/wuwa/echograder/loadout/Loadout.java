package com.wuwa.echograder.loadout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.auth.UserAccount;
import com.wuwa.echograder.score.Grade;
import com.wuwa.echograder.score.MainStat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "loadout")
public class Loadout {

    @Id
    private UUID id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_echo_main_stat", nullable = false, length = 30)
    private MainStat firstEchoMainStat;

    @Column(nullable = false, precision = 6, scale = 1)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Grade grade;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @OneToMany(mappedBy = "loadout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EchoStat> echoes = new ArrayList<>();

    protected Loadout() {
    }

    public Loadout(
            UserAccount user,
            String name,
            MainStat firstEchoMainStat,
            BigDecimal score,
            Grade grade) {
        this.user = user;
        this.name = name;
        this.firstEchoMainStat = firstEchoMainStat;
        this.score = score;
        this.grade = grade;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addEcho(EchoStat echo) {
        echoes.add(echo);
        echo.assignTo(this);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MainStat getFirstEchoMainStat() {
        return firstEchoMainStat;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Grade getGrade() {
        return grade;
    }

    public List<EchoStat> getEchoes() {
        return echoes;
    }
}

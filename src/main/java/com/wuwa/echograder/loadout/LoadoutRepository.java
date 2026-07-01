package com.wuwa.echograder.loadout;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadoutRepository extends JpaRepository<Loadout, UUID> {
}

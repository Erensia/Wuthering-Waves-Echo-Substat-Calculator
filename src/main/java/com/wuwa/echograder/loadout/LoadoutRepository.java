package com.wuwa.echograder.loadout;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadoutRepository extends JpaRepository<Loadout, UUID> {

    @EntityGraph(attributePaths = "echoes")
    List<Loadout> findAllByUserIdOrderByScoreDescNameAscCreatedAtDesc(UUID userId);

    long deleteByIdAndUserId(UUID id, UUID userId);
}

package com.wuwa.echograder.loadout;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoadoutRepository extends JpaRepository<Loadout, UUID> {

    @EntityGraph(attributePaths = "echoes")
    List<Loadout> findAllByUserIdOrderByScoreDescNameAscCreatedAtDesc(UUID userId);

    @Query("""
            select l.name as characterName,
                   max(l.score) as bestScore,
                   count(l) as loadoutCount
            from Loadout l
            where l.name is not null and trim(l.name) <> ''
            group by l.name
            order by max(l.score) desc, l.name asc
            """)
    List<CharacterScoreSummary> findCharacterScoreSummaries();

    long deleteByIdAndUserId(UUID id, UUID userId);
}

package com.wuwa.echograder.loadout;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoadoutRepository extends JpaRepository<Loadout, UUID> {

    @EntityGraph(attributePaths = "echoes")
    List<Loadout> findAllByUserIdOrderByScoreDescNameAscCreatedAtDesc(UUID userId);

    @Query("""
            select l.characterName as characterName,
                   max(l.score) as bestScore,
                   count(l) as loadoutCount
            from Loadout l
            where l.user.id = :userId
              and l.characterName is not null
              and trim(l.characterName) <> ''
            group by l.characterName
            order by max(l.score) desc, l.characterName asc
            """)
    List<CharacterScoreSummary> findCharacterScoreSummariesByUserId(@Param("userId") UUID userId);

    long deleteByIdAndUserId(UUID id, UUID userId);
}

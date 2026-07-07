package com.wuwa.echograder.loadout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.auth.UserAccount;
import com.wuwa.echograder.score.ScoreService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoadoutServiceTest {

    @Mock
    private LoadoutRepository repository;

    @Mock
    private ScoreService scoreService;

    @Mock
    private UserAccount user;

    @Test
    void deletesOnlyTheSignedInUsersLoadout() {
        UUID loadoutId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(repository.deleteByIdAndUserId(loadoutId, userId)).thenReturn(1L);

        boolean deleted = new LoadoutService(repository, scoreService).delete(loadoutId, user);

        assertThat(deleted).isTrue();
        verify(repository).deleteByIdAndUserId(loadoutId, userId);
    }

    @Test
    void reportsMissingOrUnownedLoadout() {
        UUID loadoutId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(repository.deleteByIdAndUserId(loadoutId, userId)).thenReturn(0L);

        boolean deleted = new LoadoutService(repository, scoreService).delete(loadoutId, user);

        assertThat(deleted).isFalse();
    }

    @Test
    void mapsCharacterBestScoresToDashboardResults() {
        CharacterScoreSummary summary = new CharacterScoreSummary() {
            @Override
            public String getCharacterName() {
                return "금희";
            }

            @Override
            public BigDecimal getBestScore() {
                return new BigDecimal("226.4");
            }

            @Override
            public long getLoadoutCount() {
                return 2;
            }
        };
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(repository.findCharacterScoreSummariesByUserId(userId)).thenReturn(List.of(summary));

        List<CharacterScoreResult> result =
                new LoadoutService(repository, scoreService).findCharacterScores(user);

        assertThat(result).singleElement().satisfies(character -> {
            assertThat(character.characterName()).isEqualTo("금희");
            assertThat(character.bestScore()).isEqualByComparingTo("226.4");
            assertThat(character.grade().name()).isEqualTo("COMPLETE");
            assertThat(character.gradeLabel()).isEqualTo("종결");
            assertThat(character.loadoutCount()).isEqualTo(2);
        });
    }
}

package com.wuwa.echograder.loadout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}

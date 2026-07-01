package com.wuwa.echograder.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest {

    @Mock
    private UserAccountRepository repository;

    @Test
    void searchNormalizesQueryAndReturnsOnlyPublicUserInformation() {
        UserAccount user = new UserAccount("tester", "secret-password");
        user.onCreate();
        when(repository.findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc("test"))
                .thenReturn(List.of(user));

        List<UserSearchResult> result = new UserSearchService(repository).search("  TeSt  ");

        assertThat(result).containsExactly(
                new UserSearchResult(user.getUsername(), user.getCreatedAt()));
        verify(repository).findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc("test");
    }

    @Test
    void searchRejectsBlankQuery() {
        assertThatThrownBy(() -> new UserSearchService(repository).search("  "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void searchRejectsUnsupportedCharacters() {
        assertThatThrownBy(() -> new UserSearchService(repository).search("test%"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }
}

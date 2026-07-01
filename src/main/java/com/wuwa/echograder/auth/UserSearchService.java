package com.wuwa.echograder.auth;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserSearchService {

    private static final int MAX_QUERY_LENGTH = 30;
    private static final String USERNAME_PATTERN = "^[\\p{L}\\p{N}_]+$";

    private final UserAccountRepository repository;

    public UserSearchService(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UserSearchResult> search(String query) {
        String normalizedQuery = normalizeQuery(query);
        return repository.findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(normalizedQuery).stream()
                .map(UserSearchResult::from)
                .toList();
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            throw invalidQuery();
        }

        String normalized = query.strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()
                || normalized.length() > MAX_QUERY_LENGTH
                || !normalized.matches(USERNAME_PATTERN)) {
            throw invalidQuery();
        }
        return normalized;
    }

    private ResponseStatusException invalidQuery() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "검색어는 한글, 영문, 숫자, 밑줄을 사용해 1자 이상 30자 이하로 입력해주세요.");
    }
}

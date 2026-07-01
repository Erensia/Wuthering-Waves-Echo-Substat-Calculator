package com.wuwa.echograder.web;

import java.util.List;

import com.wuwa.echograder.auth.UserSearchResult;
import com.wuwa.echograder.auth.UserSearchService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserSearchService userSearchService;

    public UserController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/search")
    public List<UserSearchResult> search(@RequestParam String query) {
        return userSearchService.search(query);
    }
}

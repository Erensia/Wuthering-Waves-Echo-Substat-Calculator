package com.wuwa.echograder.web;

import com.wuwa.echograder.score.ScoreRequest;
import com.wuwa.echograder.score.ScoreResult;
import com.wuwa.echograder.score.ScoreService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/calculate")
    public ScoreResult calculate(@Valid @RequestBody ScoreRequest request) {
        return scoreService.calculate(request);
    }
}

package com.wuwa.echograder.loadout;

import com.wuwa.echograder.score.EchoInput;
import com.wuwa.echograder.score.ScoreResult;
import com.wuwa.echograder.score.ScoreService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadoutService {

    private final LoadoutRepository repository;
    private final ScoreService scoreService;

    public LoadoutService(LoadoutRepository repository, ScoreService scoreService) {
        this.repository = repository;
        this.scoreService = scoreService;
    }

    @Transactional
    public SavedLoadoutResult save(SaveLoadoutRequest request) {
        ScoreResult result = scoreService.calculate(request.scoreRequest());
        Loadout loadout = new Loadout(
                normalizeName(request.name()),
                request.scoreRequest().firstEchoMainStat(),
                result.score(),
                result.grade());

        for (int index = 0; index < request.scoreRequest().echoes().size(); index++) {
            EchoInput echo = request.scoreRequest().echoes().get(index);
            loadout.addEcho(new EchoStat(index + 1, echo.critRate(), echo.critDamage()));
        }

        Loadout saved = repository.save(loadout);
        return new SavedLoadoutResult(saved.getId(), saved.getName(), saved.getCreatedAt(), result);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name.strip();
    }
}

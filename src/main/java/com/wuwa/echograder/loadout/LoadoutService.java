package com.wuwa.echograder.loadout;

import java.util.List;
import java.util.UUID;

import com.wuwa.echograder.auth.UserAccount;
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
    public SavedLoadoutResult save(SaveLoadoutRequest request, UserAccount user) {
        ScoreResult result = scoreService.calculate(request.scoreRequest());
        Loadout loadout = new Loadout(
                user,
                normalizeName(request.name()),
                request.scoreRequest().firstEchoMainStat(),
                result.score(),
                result.grade());

        for (int index = 0; index < request.scoreRequest().echoes().size(); index++) {
            EchoInput echo = request.scoreRequest().echoes().get(index);
            loadout.addEcho(new EchoStat(
                    index + 1,
                    echo.cost().getValue(),
                    echo.critRate(),
                    echo.critDamage()));
        }

        Loadout saved = repository.save(loadout);
        return new SavedLoadoutResult(saved.getId(), saved.getName(), saved.getCreatedAt(), result);
    }

    @Transactional(readOnly = true)
    public List<LoadoutResult> findAll(UserAccount user) {
        return repository.findAllByUserIdOrderByScoreDescNameAscCreatedAtDesc(user.getId()).stream()
                .map(LoadoutResult::from)
                .toList();
    }

    @Transactional
    public boolean delete(UUID id, UserAccount user) {
        return repository.deleteByIdAndUserId(id, user.getId()) > 0;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name.strip();
    }
}

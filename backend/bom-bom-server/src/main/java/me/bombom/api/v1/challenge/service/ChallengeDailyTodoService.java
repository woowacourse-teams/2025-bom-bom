package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyTodoService {

    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateChallengeDailyTodo(Long memberId) {
        LocalDate today = LocalDate.now();
        challengeDailyTodoRepository.insertTodayReadTodoIfMissing(memberId, today);
    }
}

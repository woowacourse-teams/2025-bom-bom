package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyTodoService {

    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Transactional
    public void updateChallengeDailyTodo(Long memberId, LocalDate today) {
        challengeDailyTodoRepository.insertTodayReadTodoIfMissing(memberId, today, ChallengeTodoType.READ.name());
    }
}

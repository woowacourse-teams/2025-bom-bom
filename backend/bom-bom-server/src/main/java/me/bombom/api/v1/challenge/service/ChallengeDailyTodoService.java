package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyTodoService {

    private final Clock clock;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Transactional
    public void updateChallengeDailyTodo(Long memberId, Long articleId) {
        LocalDate today = LocalDate.now(clock);
        if (isWeekend(today)) {
            log.info("오늘은 {}입니다. 주말에는 챌린지를 진행하지 않습니다.", today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN));
            return;
        }

        challengeDailyTodoRepository.insertTodayReadTodoIfMissing(
                memberId,
                articleId,
                today,
                ChallengeTodoType.READ.name()
        );
    }

    private boolean isWeekend(LocalDate today) {
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}

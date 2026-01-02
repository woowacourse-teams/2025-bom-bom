package me.bombom.api.v1.challenge.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.service.ChallengeProgressService;
import me.bombom.api.v1.challenge.service.ChallengeService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private static final String DAILY_CRON = "0 0 0 * * *";

    private final ChallengeService challengeService;
    private final ChallengeProgressService challengeProgressService;

    @Scheduled(cron = DAILY_CRON)
    @SchedulerLock(name = "cleanup_old_previous_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void checkSurvival() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Challenge> challenges = challengeService.getOngoingChallenges(yesterday);

        for (Challenge challenge : challenges) {
            try {
                challengeProgressService.proceedDailySurvivalCheck(challenge, yesterday);
            } catch (Exception e) {
                log.error("해당 챌린지 id에 대해 생존 처리가 실패했습니다. : {}", challenge.getId(), e);
            }
        }
    }
}

package me.bombom.api.v1.challenge.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderPhase;
import me.bombom.api.v1.challenge.service.ChallengeProgressService;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.challenge.service.ChallengeStartNotificationService;
import me.bombom.api.v1.challenge.service.ChallengeTeamService;
import me.bombom.api.v1.challenge.service.ChallengeTodoReminderNotificationService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String CHALLENGE_START_NOTIFICATION_CRON = "0 10 3 * * *";
    private static final String SEOUL_TIMEZONE = "Asia/Seoul";

    private final Clock clock;
    private final ChallengeService challengeService;
    private final ChallengeProgressService challengeProgressService;
    private final ChallengeTeamService challengeTeamService;
    private final ChallengeStartNotificationService challengeStartNotificationService;
    private final ChallengeTodoReminderNotificationService challengeTodoReminderNotificationService;

    @Scheduled(cron = DAILY_CRON, zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "check_survival", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void checkSurvival() {
        log.info("탈락 및 쉴드 사용 처리 시작");
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        List<Challenge> challenges = challengeService.getOngoingChallenges(yesterday);

        for (Challenge challenge : challenges) {
            try {
                challengeProgressService.proceedDailySurvivalCheck(challenge, yesterday);
            } catch (Exception e) {
                log.error("해당 챌린지 id에 대해 생존 처리가 실패했습니다. : {}", challenge.getId(), e);
            }
        }
    }

    @Scheduled(cron = DAILY_CRON, zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "process_ended_challenges", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void processEndedChallenges() {
        log.info("챌린지 종료 처리 및 뱃지 발급 시작");
        LocalDate today = LocalDate.now(clock);
        List<Challenge> endedChallenges = challengeService.getEndedChallengesPendingBadge(today);

        if (endedChallenges.isEmpty()) {
            log.info("종료된 챌린지가 없습니다.");
            return;
        }

        for (Challenge challenge : endedChallenges) {
            try {
                challengeService.processEndedChallenge(challenge);
            } catch (Exception e) {
                log.error("챌린지 종료 처리 중 오류 발생 - challengeId: {}", challenge.getId(), e);
            }
        }

        log.info("챌린지 종료 처리 및 뱃지 발급 완료");
    }

    @Scheduled(cron = DAILY_CRON, zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "reset_team_progress", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void resetTeamProgress() {
        LocalDate today = LocalDate.now(clock);
        log.info("팀 평균 달성률 초기화 시작 - date={}", today);
        List<Challenge> ongoingChallenges = challengeService.getOngoingChallenges(today);

        try {
            challengeTeamService.resetTeamsProgress(ongoingChallenges);
        } catch (Exception e) {
            log.error("팀 평균 달성률 초기화 중 오류 발생 - date={}", today, e);
        }
    }

    @Scheduled(cron = CHALLENGE_START_NOTIFICATION_CRON, zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "create_challenge_start_notifications", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void createChallengeStartNotifications() {
        LocalDate today = LocalDate.now(clock);
        log.info("챌린지 시작 알림 추가 시작 - date={}", today);

        try {
            challengeStartNotificationService.createPendingNotificationsForStartingChallenges(today);
        } catch (Exception e) {
            log.error("챌린지 시작 알림 적재 중 오류 발생 - date={}", today, e);
        }
    }

    @Scheduled(cron = "${challenge.scheduler.todo-reminder-first-cron}", zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "create_challenge_todo_reminder_notifications_first", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void createChallengeTodoReminderNotificationsFirst() {
        LocalDate today = LocalDate.now(clock);
        log.info("챌린지 TODO 리마인더 알림 1차 추가 시작 - date={}, phase={}", today, ChallengeTodoReminderPhase.FIRST);

        try {
            challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today,
                    ChallengeTodoReminderPhase.FIRST);
        } catch (Exception e) {
            log.error("챌린지 TODO 리마인더 알림 1차 적재 중 오류 발생 - date={}, phase={}", today, ChallengeTodoReminderPhase.FIRST, e);
        }
    }

    @Scheduled(cron = "${challenge.scheduler.todo-reminder-second-cron}", zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "create_challenge_todo_reminder_notifications_second", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void createChallengeTodoReminderNotificationsSecond() {
        LocalDate today = LocalDate.now(clock);
        log.info("챌린지 TODO 리마인더 알림 2차 추가 시작 - date={}, phase={}", today, ChallengeTodoReminderPhase.SECOND);

        try {
            challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today,
                    ChallengeTodoReminderPhase.SECOND);
        } catch (Exception e) {
            log.error("챌린지 TODO 리마인더 알림 2차 적재 중 오류 발생 - date={}, phase={}", today, ChallengeTodoReminderPhase.SECOND, e);
        }
    }
}

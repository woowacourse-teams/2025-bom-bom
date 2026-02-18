package news.bombom.notification.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.article.service.ArticleArrivalNotificationProcessor;
import news.bombom.challenge.service.ChallengeStartNotificationProcessor;
import news.bombom.challenge.service.ChallengeTodoReminderNotificationProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ArticleArrivalNotificationProcessor articleProcessor;
    private final ChallengeTodoReminderNotificationProcessor challengeProcessor;
    private final ChallengeStartNotificationProcessor challengeStartProcessor;

    @Scheduled(fixedDelay = 30000)
    public void processPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("아티클 재시도 Processor 실행");

        try {
            articleProcessor.processPendingNotifications(now);
        } catch (Exception e) {
            log.error("Processor 실행 중 오류 발생: type={}", articleProcessor.type(), e);
        }
    }

    @Scheduled(cron = "0 0 21 * * MON-FRI", zone = "Asia/Seoul")
    public void processChallengeTodoReminderNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("챌린지 TODO 리마인더 Processor 실행");

        try {
            challengeProcessor.processPendingNotifications(now);
        } catch (Exception e) {
            log.error("Processor 실행 중 오류 발생: type={}", challengeProcessor.type(), e);
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void processChallengeStartNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("챌린지 시작 알림 Processor 실행");

        try {
            challengeStartProcessor.processPendingNotifications(now);
        } catch (Exception e) {
            log.error("Processor 실행 중 오류 발생: type={}", challengeStartProcessor.type(), e);
        }
    }
}

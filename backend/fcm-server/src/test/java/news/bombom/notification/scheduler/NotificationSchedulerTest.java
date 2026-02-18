package news.bombom.notification.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import news.bombom.article.service.ArticleArrivalNotificationProcessor;
import news.bombom.challenge.service.ChallengeStartNotificationProcessor;
import news.bombom.challenge.service.ChallengeTodoReminderNotificationProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("통합 알림 스케줄러 테스트")
class NotificationSchedulerTest {

    @Mock
    private ArticleArrivalNotificationProcessor articleProcessor;

    @Mock
    private ChallengeTodoReminderNotificationProcessor challengeProcessor;

    @Mock
    private ChallengeStartNotificationProcessor challengeStartProcessor;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Test
    @DisplayName("아티클 재시도 스케줄이 아티클 Processor를 호출한다")
    void processPendingNotifications_DelegatesToArticleProcessor() {
        notificationScheduler.processPendingNotifications();

        verify(articleProcessor, times(1)).processPendingNotifications(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("챌린지 스케줄이 챌린지 Processor를 호출한다")
    void processChallengeTodoReminderNotifications_DelegatesToChallengeProcessor() {
        notificationScheduler.processChallengeTodoReminderNotifications();

        verify(challengeProcessor, times(1)).processPendingNotifications(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("챌린지 시작 알림 스케줄이 챌린지 시작 Processor를 호출한다")
    void processChallengeStartNotifications_DelegatesToChallengeStartProcessor() {
        notificationScheduler.processChallengeStartNotifications();

        verify(challengeStartProcessor, times(1)).processPendingNotifications(org.mockito.ArgumentMatchers.any());
    }
}

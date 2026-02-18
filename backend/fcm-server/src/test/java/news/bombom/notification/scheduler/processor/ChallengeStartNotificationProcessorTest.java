package news.bombom.notification.scheduler.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import news.bombom.challenge.domain.ChallengeStartNotification;
import news.bombom.challenge.repository.ChallengeStartNotificationRepository;
import news.bombom.challenge.service.ChallengeStartNotificationProcessor;
import news.bombom.challenge.service.ChallengeStartNotificationStatusService;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.service.NotificationProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("챌린지 시작 알림 Processor 테스트")
class ChallengeStartNotificationProcessorTest {

    @Mock
    private ChallengeStartNotificationRepository notificationRepository;

    @Mock
    private NotificationProcessingService notificationProcessingService;

    @Mock
    private ChallengeStartNotificationStatusService challengeStatusService;

    @InjectMocks
    private ChallengeStartNotificationProcessor processor;

    @Test
    @DisplayName("호출 시점의 대기 알림을 조회하고 처리한다")
    void processPendingNotifications_ProcessesNotifications() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 8, 0);
        ChallengeStartNotification notification = createNotification();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));

        processor.processPendingNotifications(now);

        verify(notificationRepository, times(1))
                .findRetryCandidates(List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), now);
        verify(notificationProcessingService, times(1)).processNotification(
                notification,
                NotificationCategory.CHALLENGE_START,
                challengeStatusService
        );
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 알림은 건너뛴다")
    void processPendingNotifications_ExceededRetry_SkipsProcessing() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 8, 0);

        ChallengeStartNotification exceeded = ChallengeStartNotification.builder()
                .memberId(1L)
                .challengeId(99L)
                .challengeName("챌린지")
                .status(NotificationStatus.FAILED)
                .attempts(4)
                .build();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(Collections.singletonList(exceeded));

        processor.processPendingNotifications(now);

        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
    }

    private ChallengeStartNotification createNotification() {
        return ChallengeStartNotification.builder()
                .memberId(1L)
                .challengeId(99L)
                .challengeName("챌린지")
                .build();
    }
}

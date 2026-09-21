package news.bombom.inquiry.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.repository.InquiryMessageArrivalNotificationRepository;
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
@DisplayName("문의 답변 알림 Processor 테스트")
class InquiryMessageArrivalNotificationProcessorTest {

    @Mock
    private InquiryMessageArrivalNotificationRepository notificationRepository;

    @Mock
    private NotificationProcessingService notificationProcessingService;

    @Mock
    private InquiryMessageArrivalNotificationStatusService statusService;

    @InjectMocks
    private InquiryMessageArrivalNotificationProcessor processor;

    @Test
    @DisplayName("호출 시점의 대기 알림을 조회하고 처리한다")
    void processPendingNotifications_ProcessesNotifications() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);
        InquiryMessageArrivalNotification notification = createNotification();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));

        processor.processPendingNotifications(now);

        verify(notificationRepository, times(1))
                .findRetryCandidates(List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), now);
        verify(notificationProcessingService, times(1)).processNotification(
                notification,
                NotificationCategory.INQUIRY_MESSAGE_ARRIVAL,
                statusService
        );
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 알림은 건너뛴다")
    void processPendingNotifications_ExceededRetry_SkipsProcessing() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);

        InquiryMessageArrivalNotification exceeded = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .roomId(10L)
                .content("답변 내용")
                .status(NotificationStatus.FAILED)
                .attempts(3)
                .build();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(Collections.singletonList(exceeded));

        processor.processPendingNotifications(now);

        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
    }

    private InquiryMessageArrivalNotification createNotification() {
        return InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .roomId(10L)
                .content("답변 내용")
                .build();
    }
}

package news.bombom.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import news.bombom.inquiry.domain.InquiryMessage;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.domain.InquiryRoom;
import news.bombom.inquiry.repository.InquiryMessageArrivalNotificationRepository;
import news.bombom.inquiry.repository.InquiryMessageRepository;
import news.bombom.inquiry.repository.InquiryRoomRepository;
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
    private InquiryRoomRepository inquiryRoomRepository;

    @Mock
    private InquiryMessageRepository inquiryMessageRepository;

    @Mock
    private NotificationProcessingService notificationProcessingService;

    @Mock
    private InquiryMessageArrivalNotificationStatusService statusService;

    @InjectMocks
    private InquiryMessageArrivalNotificationProcessor processor;

    @Test
    @DisplayName("아직 읽지 않은 알림은 내용을 채워 발송을 요청한다")
    void processPendingNotifications_Unread_ProcessesWithContent() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);
        InquiryMessageArrivalNotification notification = createNotification(10L, 100L);
        InquiryRoom room = createRoom(10L, null);

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));
        when(inquiryRoomRepository.findAllById(List.of(10L))).thenReturn(List.of(room));
        when(inquiryMessageRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(createMessage(100L, "답변 내용")));

        processor.processPendingNotifications(now);

        verify(notificationRepository, times(1))
                .findRetryCandidates(List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), now);
        verify(notificationProcessingService, times(1)).processNotification(
                notification,
                NotificationCategory.INQUIRY_MESSAGE_ARRIVAL,
                statusService
        );
        assertThat(notification.getContent()).isEqualTo("답변 내용");
    }

    @Test
    @DisplayName("이미 읽은 알림은 삭제하고 발송하지 않는다")
    void processPendingNotifications_AlreadyRead_DeletesWithoutProcessing() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);
        InquiryMessageArrivalNotification notification = createNotification(10L, 100L);
        InquiryRoom room = createRoom(10L, 100L);

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));
        when(inquiryRoomRepository.findAllById(List.of(10L))).thenReturn(List.of(room));

        processor.processPendingNotifications(now);

        verify(statusService, times(1)).deleteAlreadyRead(notification);
        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 알림은 건너뛴다")
    void processPendingNotifications_ExceededRetry_SkipsProcessing() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);

        InquiryMessageArrivalNotification exceeded = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .roomId(10L)
                .messageId(100L)
                .status(NotificationStatus.FAILED)
                .attempts(3)
                .build();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(Collections.singletonList(exceeded));

        processor.processPendingNotifications(now);

        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
        verify(statusService, never()).deleteAlreadyRead(any());
    }

    private InquiryMessageArrivalNotification createNotification(Long roomId, Long messageId) {
        return InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .roomId(roomId)
                .messageId(messageId)
                .build();
    }

    private InquiryRoom createRoom(Long id, Long lastReadMessageIdByUser) {
        return InquiryRoom.builder()
                .id(id)
                .lastReadMessageIdByUser(lastReadMessageIdByUser)
                .build();
    }

    private InquiryMessage createMessage(Long id, String content) {
        return InquiryMessage.builder()
                .id(id)
                .content(content)
                .build();
    }
}

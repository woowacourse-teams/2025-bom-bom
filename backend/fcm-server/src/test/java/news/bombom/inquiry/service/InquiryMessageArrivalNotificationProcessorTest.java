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
    @DisplayName("아직 읽지 않은 알림은 메시지의 roomId와 content를 채워 발송을 요청한다")
    void 아직_읽지_않은_알림은_메시지의_roomId와_content를_채워_발송을_요청한다() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);
        InquiryMessageArrivalNotification notification = createNotification(100L);
        InquiryMessage message = createMessage(100L, 10L, "답변 내용");
        InquiryRoom room = createRoom(10L, null);

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));
        when(inquiryMessageRepository.findAllById(List.of(100L))).thenReturn(List.of(message));
        when(inquiryRoomRepository.findAllById(List.of(10L))).thenReturn(List.of(room));

        processor.processPendingNotifications(now);

        verify(notificationRepository, times(1))
                .findRetryCandidates(List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), now);
        verify(notificationProcessingService, times(1)).processNotification(
                notification,
                NotificationCategory.INQUIRY_MESSAGE_ARRIVAL,
                statusService
        );
        assertThat(notification.getRoomId()).isEqualTo(10L);
        assertThat(notification.getContent()).isEqualTo("답변 내용");
    }

    @Test
    @DisplayName("이미 읽은 알림은 삭제하고 발송하지 않는다")
    void 이미_읽은_알림은_삭제하고_발송하지_않는다() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);
        InquiryMessageArrivalNotification notification = createNotification(100L);
        InquiryMessage message = createMessage(100L, 10L, "답변 내용");
        InquiryRoom room = createRoom(10L, 100L);

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(List.of(notification));
        when(inquiryMessageRepository.findAllById(List.of(100L))).thenReturn(List.of(message));
        when(inquiryRoomRepository.findAllById(List.of(10L))).thenReturn(List.of(room));

        processor.processPendingNotifications(now);

        verify(statusService, times(1)).deleteAlreadyRead(notification);
        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 알림은 건너뛴다")
    void 최대_재시도_횟수_초과_알림은_건너뛴다() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 16, 10, 0);

        InquiryMessageArrivalNotification exceeded = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(100L)
                .status(NotificationStatus.FAILED)
                .attempts(3)
                .build();

        when(notificationRepository.findRetryCandidates(anyList(), any()))
                .thenReturn(Collections.singletonList(exceeded));
        when(inquiryMessageRepository.findAllById(List.of(100L))).thenReturn(List.of());

        processor.processPendingNotifications(now);

        verify(notificationProcessingService, never()).processNotification(any(), any(), any());
        verify(statusService, never()).deleteAlreadyRead(any());
    }

    private InquiryMessageArrivalNotification createNotification(Long messageId) {
        return InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(messageId)
                .build();
    }

    private InquiryRoom createRoom(Long id, Long lastReadMessageIdByUser) {
        return InquiryRoom.builder()
                .id(id)
                .lastReadMessageIdByUser(lastReadMessageIdByUser)
                .build();
    }

    private InquiryMessage createMessage(Long id, Long roomId, String content) {
        return InquiryMessage.builder()
                .id(id)
                .roomId(roomId)
                .content(content)
                .build();
    }
}

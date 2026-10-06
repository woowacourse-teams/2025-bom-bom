package news.bombom.inquiry.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.inquiry.domain.InquiryMessage;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.domain.InquiryRoom;
import news.bombom.inquiry.repository.InquiryMessageArrivalNotificationRepository;
import news.bombom.inquiry.repository.InquiryMessageRepository;
import news.bombom.inquiry.repository.InquiryRoomRepository;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.scheduler.NotificationProcessor;
import news.bombom.notification.service.NotificationProcessingService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryMessageArrivalNotificationProcessor implements NotificationProcessor {

    private final InquiryMessageArrivalNotificationRepository notificationRepository;
    private final InquiryRoomRepository inquiryRoomRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final NotificationProcessingService notificationProcessingService;
    private final InquiryMessageArrivalNotificationStatusService statusService;

    @Override
    public String type() {
        return NotificationCategory.INQUIRY_MESSAGE_ARRIVAL.name();
    }

    @Override
    public void processPendingNotifications(LocalDateTime now) {
        List<InquiryMessageArrivalNotification> pendingNotifications =
                notificationRepository.findRetryCandidates(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        now
                );

        log.info("[{}] 처리할 알림 개수: {}", type(), pendingNotifications.size());

        Map<Long, InquiryMessage> messagesByMessageId = findMessagesByMessageId(pendingNotifications);
        Map<Long, InquiryRoom> roomsByRoomId = findRoomsByRoomId(messagesByMessageId.values());

        for (InquiryMessageArrivalNotification notification : pendingNotifications) {
            try {
                if (!notification.shouldRetry()) {
                    log.warn("[{}] 최대 재시도 횟수 초과로 처리 중단: notificationId={}, attempts={}",
                            type(), notification.getId(), notification.getAttempts());
                    continue;
                }

                InquiryMessage message = messagesByMessageId.get(notification.getMessageId());
                InquiryRoom room = message == null ? null : roomsByRoomId.get(message.getRoomId());
                if (room != null && room.hasRead(notification.getMessageId())) {
                    statusService.deleteAlreadyRead(notification);
                    continue;
                }

                notification.assignRoomId(message == null ? null : message.getRoomId());
                notification.assignContent(message == null ? null : message.getContent());
                notificationProcessingService.processNotification(
                        notification,
                        NotificationCategory.INQUIRY_MESSAGE_ARRIVAL,
                        statusService
                );
            } catch (Exception e) {
                log.error("[{}] 알림 처리 중 오류 발생: notificationId={}", type(), notification.getId(), e);
            }
        }
    }

    private Map<Long, InquiryMessage> findMessagesByMessageId(List<InquiryMessageArrivalNotification> notifications) {
        List<Long> messageIds = notifications.stream()
                .map(InquiryMessageArrivalNotification::getMessageId)
                .distinct()
                .toList();

        return inquiryMessageRepository.findAllById(messageIds).stream()
                .collect(Collectors.toMap(InquiryMessage::getId, Function.identity()));
    }

    private Map<Long, InquiryRoom> findRoomsByRoomId(Collection<InquiryMessage> messages) {
        List<Long> roomIds = messages.stream()
                .map(InquiryMessage::getRoomId)
                .distinct()
                .toList();

        return inquiryRoomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(InquiryRoom::getId, Function.identity()));
    }
}

package news.bombom.inquiry.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import news.bombom.inquiry.domain.InquiryMessage;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.repository.InquiryMessageRepository;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryMessageArrivalMessageBuilder implements NotificationMessageBuilder {

    private static final String TITLE = "문의에 대한 답변이 도착했어요!";

    private final InquiryMessageRepository inquiryMessageRepository;

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof InquiryMessageArrivalNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        InquiryMessageArrivalNotification inquiry = (InquiryMessageArrivalNotification) notification;
        InquiryMessage message = inquiryMessageRepository.findById(inquiry.getMessageId())
                .orElseThrow(() -> new IllegalStateException("문의 메시지를 찾을 수 없습니다: messageId=" + inquiry.getMessageId()));

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(TITLE)
                .content(message.getContent())
                .type(NotificationType.FCM)
                .data(Map.of(
                        "roomId", String.valueOf(message.getRoomId()),
                        "notificationType", NotificationPayloadType.INQUIRY_MESSAGE_ARRIVAL
                ))
                .build();
    }
}

package news.bombom.notification.service.message;

import java.util.Map;
import news.bombom.notification.domain.ChallengeTodoReminderNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import org.springframework.stereotype.Component;

@Component
public class ChallengeTodoReminderMessageBuilder implements NotificationMessageBuilder {

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof ChallengeTodoReminderNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        ChallengeTodoReminderNotification remind = (ChallengeTodoReminderNotification) notification;

        String title = remind.getChallengeName() + " 아직 할 일이 남았어요!";
        String content = "오늘도 기록을 채워볼까요? 지금 참여해서 꾸준하게 이어가요!";

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(title)
                .content(content)
                .type(NotificationType.FCM)
                .data(Map.of(
                        "notificationType", NotificationPayloadType.CHALLENGE_TODO_REMINDER
                ))
                .build();
    }
}

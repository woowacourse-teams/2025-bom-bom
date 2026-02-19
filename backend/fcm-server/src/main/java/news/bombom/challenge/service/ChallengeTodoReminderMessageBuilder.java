package news.bombom.challenge.service;

import java.util.Map;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.domain.ChallengeTodoReminderPhase;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
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

        String title;
        String content;
        if (remind.getPhase() == ChallengeTodoReminderPhase.SECOND) {
            title = "[" + remind.getChallengeName() + "] 하루가 지나기 한 시간 전!";
            content = "5분만 읽으면 오늘 출석이예요. 가볍게 완료해봐요!";
        } else {
            title = remind.getChallengeName() + " 아직 할 일이 남았어요!";
            content = "오늘도 기록을 채워볼까요? 지금 참여해서 꾸준하게 이어가요!";
        }

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(title)
                .content(content)
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(remind.getChallengeId()),
                        "notificationType", NotificationPayloadType.DEFAULT
                ))
                .build();
    }
}

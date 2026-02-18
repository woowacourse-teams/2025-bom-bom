package news.bombom.challenge.service;

import java.util.Map;
import news.bombom.challenge.domain.ChallengeStartNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ChallengeStartMessageBuilder implements NotificationMessageBuilder {

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof ChallengeStartNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        ChallengeStartNotification startNotification = (ChallengeStartNotification) notification;
        String title = startNotification.getChallengeName() + " 오늘부터 시작!";
        String content = "1일차가 열렸어요 ✅ 오늘 미션부터 가볍게 출발해요!";

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(title)
                .content(content)
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(startNotification.getChallengeId()),
                        "notificationType", NotificationPayloadType.CHALLENGE_START
                ))
                .build();
    }
}

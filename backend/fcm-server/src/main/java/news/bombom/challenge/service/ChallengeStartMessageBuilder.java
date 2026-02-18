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

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(startNotification.getChallengeName() + " 챌린지가 시작됐어요!")
                .content("오늘부터 챌린지를 시작해 볼까요? ✅")
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(startNotification.getChallengeId()),
                        "notificationType", NotificationPayloadType.CHALLENGE_START
                ))
                .build();
    }
}

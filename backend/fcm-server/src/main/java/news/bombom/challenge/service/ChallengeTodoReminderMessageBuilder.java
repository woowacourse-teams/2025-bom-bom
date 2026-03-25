package news.bombom.challenge.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.domain.ChallengeTodoReminderPhase;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
import org.springframework.stereotype.Component;

import static news.bombom.challenge.service.ChallengeTodoReminderMessageTemplates.*;

@Component
public class ChallengeTodoReminderMessageBuilder implements NotificationMessageBuilder {

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof ChallengeTodoReminderNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        ChallengeTodoReminderNotification remind = (ChallengeTodoReminderNotification) notification;
        boolean isFirst = remind.getPhase() == ChallengeTodoReminderPhase.FIRST;
        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(buildTitle(remind, isFirst))
                .content(buildBody(remind))
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(remind.getChallengeId()),
                        "notificationType", NotificationPayloadType.DEFAULT
                ))
                .build();
    }

    private String buildTitle(ChallengeTodoReminderNotification notification, boolean isFirst) {
        int streak = notification.getStreak();
        String template;

        if (notification.isLastDay()) template = isFirst ? TITLE_LAST_DAY_FIRST : TITLE_LAST_DAY_SECOND;
        else if (streak >= 21)  template = isFirst ? TITLE_21_FIRST : TITLE_21_SECOND;
        else if (streak >= 14)  template = isFirst ? TITLE_14_FIRST : TITLE_14_SECOND;
        else if (streak >= 7)   template = isFirst ? TITLE_7_FIRST  : TITLE_7_SECOND;
        else if (streak >= 3)   template = isFirst ? TITLE_3_FIRST  : TITLE_3_SECOND;
        else if (streak >= 2)   template = isFirst ? TITLE_2_FIRST : TITLE_2_SECOND;
        else                    template = isFirst ? TITLE_0_FIRST  : TITLE_0_SECOND;

        return String.format(template, notification.getChallengeName(), streak);
    }

    private String buildBody(ChallengeTodoReminderNotification notification) {
        int streak = notification.getStreak();
        if (notification.isLastDay()) {
            return pickRandom(LAST_DAY_BODY);
        }

        return String.format(pickRandom(BODY_POOL), streak, streak + 1);
    }

    private String pickRandom(List<String> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}

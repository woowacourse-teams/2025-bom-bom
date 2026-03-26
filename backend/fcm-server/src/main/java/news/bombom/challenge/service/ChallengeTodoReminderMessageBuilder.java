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
                .content(buildBody(remind, isFirst))
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(remind.getChallengeId()),
                        "notificationType", NotificationPayloadType.DEFAULT
                ))
                .build();
    }

    private String buildTitle(ChallengeTodoReminderNotification n, boolean isFirst) {
        int streak = n.getStreak();
        Integer daysSince = n.getDaysSinceLastParticipation();
        String name = n.getChallengeName();

        // 남은 결석 허용 횟수가 0
        if (n.getRemainingAbsences() == 0) {
            return formatByPhase(isFirst, TITLE_ELIMINATION_RISK_FIRST, TITLE_ELIMINATION_RISK_SECOND, name);
        }
        // 챌린지 마지막 날
        if (n.isLastDay()) {
            return formatByPhase(isFirst, TITLE_LAST_DAY_FIRST, TITLE_LAST_DAY_SECOND, name);
        }
        // 스트릭이 0이면서 참여 이력이 있는 사람. 남은 결석 허용 횟수 알려주며 다시 참여 유도
        if (streak == 0 && daysSince != null && daysSince > 0) {
            return formatByPhase(isFirst, TITLE_ABSENT_FIRST, TITLE_ABSENT_SECOND, name, daysSince);
        }

        if (streak >= 21) return formatByPhase(isFirst, TITLE_21_FIRST, TITLE_21_SECOND, name, streak);
        if (streak >= 14) return formatByPhase(isFirst, TITLE_14_FIRST, TITLE_14_SECOND, name, streak);
        if (streak >= 7) return formatByPhase(isFirst, TITLE_7_FIRST,  TITLE_7_SECOND,  name, streak);
        if (streak >= 3) return formatByPhase(isFirst, TITLE_3_FIRST,  TITLE_3_SECOND,  name, streak);
        if (streak >= 2) return formatByPhase(isFirst, TITLE_2_FIRST,  TITLE_2_SECOND,  name, streak);
        return formatByPhase(isFirst, TITLE_0_FIRST, TITLE_0_SECOND, name, streak); // 참여 이력 없는 사람
    }

    private String buildBody(ChallengeTodoReminderNotification n, boolean isFirst) {
        int streak = n.getStreak();
        Integer daysSince = n.getDaysSinceLastParticipation();
        int remaining = n.getRemainingAbsences();

        if (remaining == 0) { // 남은 결석 허용 횟수가 0
            return isFirst ? BODY_ELIMINATION_RISK_FIRST : BODY_ELIMINATION_RISK_SECOND;
        }
        if (n.isLastDay()) { // 챌린지 마지막 날
            return pickRandom(LAST_DAY_BODY);
        }
        if (streak == 0 && daysSince != null && daysSince > 0) {//스트릭이 0이면서 참여 이력이 있는 사람.남은 결석 허용 횟수 알려주며 다시 참여 유도
            return formatByPhase(isFirst, BODY_ABSENT_FIRST, BODY_ABSENT_SECOND, remaining);
        }
        return String.format(pickRandom(BODY_POOL), streak, streak + 1);
    }

    private String formatByPhase(boolean isFirst, String first, String second, Object... args) {
        return String.format(isFirst ? first : second, args);
    }

    private String pickRandom(List<String> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}

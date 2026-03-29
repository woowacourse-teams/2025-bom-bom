package news.bombom.challenge.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.service.ReminderMessageProperties.PhaseMessage;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeTodoReminderMessageBuilder implements NotificationMessageBuilder {

    private final ReminderMessageProperties props;

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof ChallengeTodoReminderNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        ChallengeTodoReminderNotification remind = (ChallengeTodoReminderNotification) notification;
        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(buildTitle(remind))
                .content(buildBody(remind))
                .type(NotificationType.FCM)
                .data(Map.of(
                        "challengeId", String.valueOf(remind.getChallengeId()),
                        "notificationType", NotificationPayloadType.DEFAULT
                ))
                .build();
    }

    private String buildTitle(ChallengeTodoReminderNotification notification) {
        boolean isFirst = notification.isFirst();
        int streak = notification.getStreak();
        Integer daysSince = notification.getDaysSinceLastParticipation();
        String name = notification.getChallengeName();

        if (notification.getRemainingAbsences() == 0) {
            return format(props.getEliminationRisk().getTitle().byPhase(isFirst), name);
        }
        if (notification.isLastDay()) {
            return format(props.getLastDay().getTitle().byPhase(isFirst), name);
        }
        if (streak == 0 && daysSince != null && daysSince > 0) {
            return format(props.getAbsent().getTitle().byPhase(isFirst), name, daysSince);
        }

        PhaseMessage streakTitle = resolveStreakTitle(streak);
        return format(streakTitle.byPhase(isFirst), name, streak);
    }

    private String buildBody(ChallengeTodoReminderNotification notification) {
        boolean isFirst = notification.isFirst();
        int streak = notification.getStreak();
        Integer daysSince = notification.getDaysSinceLastParticipation();

        if (notification.getRemainingAbsences() == 0) {
            return props.getEliminationRisk().getBody().byPhase(isFirst);
        }
        if (notification.isLastDay()) {
            return pickRandom(props.getLastDay().getBodyPool());
        }
        if (streak == 0 && daysSince != null && daysSince > 0) {
            return format(props.getAbsent().getBody().byPhase(isFirst), notification.getRemainingAbsences());
        }
        return format(pickRandom(props.getStreak().getBodyPool()), streak, streak + 1);
    }

    private PhaseMessage resolveStreakTitle(int streak) {
        ReminderMessageProperties.Streak.StreakTitle title = props.getStreak().getTitle();
        if (streak >= 21) return title.getS21();
        if (streak >= 14) return title.getS14();
        if (streak >= 7)  return title.getS7();
        if (streak >= 3)  return title.getS3();
        if (streak >= 2)  return title.getS2();
        return title.getS0();
    }

    private String format(String template, Object... args) {
        return String.format(template, args);
    }

    private String pickRandom(List<String> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}

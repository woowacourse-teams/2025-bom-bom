package news.bombom.challenge.service;

import java.util.HashMap;
import java.util.Map;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;

public record ReminderMessageParams(

        String challengeName,
        int streak,
        int nextStreak,
        int remainingAbsences,
        Integer daysSince
) {

    public static ReminderMessageParams from(ChallengeTodoReminderNotification notification) {
        return new ReminderMessageParams(
                notification.getChallengeName(),
                notification.getStreak(),
                notification.getStreak() + 1,
                notification.getRemainingAbsences(),
                notification.getDaysSinceLastParticipation()
        );
    }

    public Map<String, Object> getReminderParams() {
        Map<String, Object> map = new HashMap<>();
        map.put("challengeName", challengeName);
        map.put("streak", streak);
        map.put("nextStreak", nextStreak);
        map.put("remainingAbsences", remainingAbsences);
        if (daysSince != null) {
            map.put("daysSince", daysSince);
        }
        return map;
    }
}

package news.bombom.notification.scheduler;

import java.time.LocalDateTime;

public interface NotificationProcessor {

    String type();

    void processPendingNotifications(LocalDateTime now);
}

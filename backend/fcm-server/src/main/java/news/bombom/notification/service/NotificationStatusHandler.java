package news.bombom.notification.service;

import news.bombom.notification.domain.Notification;
import news.bombom.notification.dto.response.NotificationResultResponse;

public interface NotificationStatusHandler<T extends Notification> {

    void updateStatus(T notification, NotificationResultResponse result);

    void markAsFailed(T notification, String reason);
}

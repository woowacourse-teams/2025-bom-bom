package news.bombomemail.email.notification;

import news.bombomemail.email.dto.NotificationMessage;

public interface NotificationPublisher {

    void publish(Object body);
}

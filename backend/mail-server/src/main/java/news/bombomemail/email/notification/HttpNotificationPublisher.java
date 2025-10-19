package news.bombomemail.email.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpNotificationPublisher implements NotificationPublisher {

    private final NotificationHttpClient notificationHttpClient;

    @Value("")
    private String notificationServerUrl;

    @Override
    public void publish(Object body) {
        notificationHttpClient.post(notificationServerUrl, body);
    }
}

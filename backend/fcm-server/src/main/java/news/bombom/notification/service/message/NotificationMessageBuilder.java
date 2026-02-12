package news.bombom.notification.service.message;

import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.dto.NotificationMessage;

public interface NotificationMessageBuilder {

    boolean supports(Notification notification);

    NotificationMessage build(Notification notification, MemberFcmToken token);
}

package news.bombom.notification.service;

import news.bombom.notification.domain.Notification;
import news.bombom.notification.dto.response.NotificationResultResponse;

/**
 * 전달되는 notification은 트랜잭션 없는 Processor에서 조회되어 넘어오는 detached 엔티티입니다. 구현체에서 필드를 변경(markSent 등)해도 dirty checking이 적용되지
 * 않으므로, 반드시 repository.save(notification)으로 merge해서 변경을 반영해야 합니다.
 */
public interface NotificationStatusHandler<T extends Notification> {

    void updateStatus(T notification, NotificationResultResponse result);

    void markAsFailed(T notification, String reason);

    void handleRejected(T notification);
}

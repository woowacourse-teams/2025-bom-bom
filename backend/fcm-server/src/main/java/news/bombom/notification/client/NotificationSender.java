package news.bombom.notification.client;

import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.domain.NotificationType;

public interface NotificationSender {
    
    /**
     * 알림 메시지를 전송합니다
     * 
     * @param message 전송할 알림 메시지
     * @return 전송 결과
     */
    NotificationResult send(NotificationMessage message);
    
    /**
     * 이 구현체가 지원하는 알림 타입을 반환합니다
     * 
     * @return 지원하는 알림 타입
     */
    NotificationType getSupportedType();
}

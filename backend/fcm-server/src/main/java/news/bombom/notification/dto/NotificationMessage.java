package news.bombom.notification.dto;

import lombok.Builder;
import lombok.Getter;
import news.bombom.notification.domain.NotificationType;

import java.util.Map;

/**
 * 범용적인 알림 메시지 모델
 * 다양한 알림 서비스에서 사용할 수 있는 공통 메시지 구조
 */
@Getter
@Builder
public class NotificationMessage {
    
    /**
     * 수신자 식별자 (FCM token, 이메일 주소, 전화번호 등)
     */
    private final String recipient;
    
    /**
     * 알림 제목
     */
    private final String title;
    
    /**
     * 알림 내용
     */
    private final String content;
    
    /**
     * 알림 타입 (FCM, SMS, EMAIL 등)
     */
    private final NotificationType type;
    
    /**
     * 추가 데이터 (articleId, notificationType 등)
     */
    private final Map<String, Object> data;
    
    /**
     * 우선순위 (높을수록 중요)
     */
    @Builder.Default
    private final int priority = 1;
    
    /**
     * 만료 시간 (밀리초)
     */
    @Builder.Default
    private final long ttl = 86400000L; // 24시간
}

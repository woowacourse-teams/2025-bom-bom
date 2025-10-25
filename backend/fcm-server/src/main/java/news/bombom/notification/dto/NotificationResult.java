package news.bombom.notification.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 알림 전송 결과 모델
 */
@Getter
@Builder
public class NotificationResult {
    
    /**
     * 전송 성공 여부
     */
    private final boolean success;
    
    /**
     * 메시지 ID (서비스별 고유 식별자)
     */
    private final String messageId;
    
    /**
     * 에러 메시지 (실패 시)
     */
    private final String errorMessage;
    
    /**
     * 전송 시간 (밀리초)
     */
    private final long timestamp;
    
    /**
     * 성공 결과 생성
     */
    public static NotificationResult success(String messageId) {
        return NotificationResult.builder()
                .success(true)
                .messageId(messageId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 실패 결과 생성
     */
    public static NotificationResult failure(String errorMessage) {
        return NotificationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

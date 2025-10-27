package news.bombom.notification.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 알림 전송 결과 모델
 */
@Getter
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

    @Builder
    public NotificationResult(boolean success, String messageId, String errorMessage, long timestamp) {
        this.success = success;
        this.messageId = messageId;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    /**
     * 성공 결과 생성
     */
    public static NotificationResult success(String messageId) {
        return new NotificationResult(true, messageId, null, System.currentTimeMillis());
    }

    /**
     * 실패 결과 생성
     */
    public static NotificationResult failure(String errorMessage) {
        return new NotificationResult(false, null, errorMessage, System.currentTimeMillis());
    }
}

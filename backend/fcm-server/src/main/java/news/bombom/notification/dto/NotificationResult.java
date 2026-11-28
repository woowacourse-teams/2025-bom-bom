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
     * 에러 코드 (실패 시, FCM MessagingErrorCode 등. 없으면 null)
     */
    private final String errorCode;

    /**
     * 전송 시간 (밀리초)
     */
    private final long timestamp;

    @Builder
    public NotificationResult(boolean success, String messageId, String errorMessage, String errorCode,
                              long timestamp) {
        this.success = success;
        this.messageId = messageId;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }

    /**
     * 성공 결과 생성
     */
    public static NotificationResult success(String messageId) {
        return new NotificationResult(true, messageId, null, null, System.currentTimeMillis());
    }

    /**
     * 실패 결과 생성
     */
    public static NotificationResult failure(String errorMessage) {
        return failure(errorMessage, null);
    }

    /**
     * 에러 코드를 포함한 실패 결과 생성
     */
    public static NotificationResult failure(String errorMessage, String errorCode) {
        return new NotificationResult(false, null, errorMessage, errorCode, System.currentTimeMillis());
    }
}

package me.bombom.api.v1.common.exception;

/**
 * 일시적인 오류로 재시도 가능한 예외
 * - 네트워크 타임아웃
 * - 서버 5xx 에러
 * - Playwright 실행 오류 (브라우저 크래시 등)
 */
public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}

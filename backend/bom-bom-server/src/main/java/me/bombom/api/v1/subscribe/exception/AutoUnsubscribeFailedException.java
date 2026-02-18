package me.bombom.api.v1.subscribe.exception;

import lombok.Getter;

/**
 * 구독 자동 취소 실패 예외
 * 
 * 다음 케이스에서 발생:
 * - 버튼/링크를 찾지 못함 (패턴 불일치)
 * - 에러 다이얼로그 감지 (사이트 에러)
 * - HTTP 4xx 클라이언트 에러
 * 
 * 재시도 불가. 디스코드 알림 후 사용자에게 수동 취소 안내.
 * 
 * 참고: "시스템 점검 중" 등 일시적 에러도 포함될 수 있으나,
 * 자동 구분이 불가능하므로 수동 개입으로 처리.
 */
@Getter
public class AutoUnsubscribeFailedException extends RuntimeException {

    private final Long newsletterId;
    private final String url;

    public AutoUnsubscribeFailedException(String message, Long newsletterId, String url) {
        super(message);
        this.newsletterId = newsletterId;
        this.url = url;
    }
}

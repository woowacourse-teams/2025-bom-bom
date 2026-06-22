package me.bombom.support.acceptance;

/**
 * 인수 테스트에서 인증 회원을 지정할 때 사용하는 전용 HTTP 헤더 이름을 모은다.
 */
public final class AcceptanceTestHeaders {

    public static final String MEMBER_ID = "X-Test-Member-Id";

    private AcceptanceTestHeaders() {
    }
}

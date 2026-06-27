package me.bombom.support.testdouble;

/**
 * 테스트 메서드 사이에 내부 상태를 초기화해야 하는 테스트 대역의 공통 계약이다.
 */
public interface ResettableTestDouble {

    void reset();
}

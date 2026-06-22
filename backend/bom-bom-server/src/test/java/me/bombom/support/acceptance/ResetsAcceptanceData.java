package me.bombom.support.acceptance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 데이터가 변경되는 인수 테스트 메서드 이후 기본 데이터셋을 다시 적재하도록 표시한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResetsAcceptanceData {
}

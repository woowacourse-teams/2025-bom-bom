package me.bombom.support.acceptance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 특정 인수 테스트 메서드에서 기본 데이터셋에 추가로 적재할 데이터셋을 지정한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalAcceptanceDataSet {

    String[] value();
}

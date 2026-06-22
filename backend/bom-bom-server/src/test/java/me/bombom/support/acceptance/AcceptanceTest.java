package me.bombom.support.acceptance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.bombom.support.integration.IntegrationTest;
import org.springframework.test.context.TestExecutionListeners;

/**
 * 지정한 데이터셋과 MockMvc RestAssured 설정을 함께 적용하는 HTTP 인수 테스트용 메타 애노테이션이다.
 */
@IntegrationTest
@Target(ElementType.TYPE)
@TestExecutionListeners(
        listeners = AcceptanceTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptanceTest {

    String[] value();
}

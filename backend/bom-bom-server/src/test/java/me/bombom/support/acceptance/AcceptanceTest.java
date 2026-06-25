package me.bombom.support.acceptance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.bombom.support.integration.IntegrationTestConfig;
import me.bombom.support.integration.TestcontainerConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

/**
 * 지정한 데이터셋과 실제 HTTP RestAssured 설정을 함께 적용하는 HTTP 인수 테스트용 메타 애노테이션이다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
@Import({TestcontainerConfig.class, IntegrationTestConfig.class, AcceptanceTestConfiguration.class})
@Target(ElementType.TYPE)
@TestExecutionListeners(
        listeners = AcceptanceTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptanceTest {

    String[] value();
}

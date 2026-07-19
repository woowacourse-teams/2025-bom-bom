package me.bombom.support.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.bombom.support.acceptance.AcceptanceTestConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

/**
 * Spring Boot, Testcontainers 설정을 함께 올리는 통합 테스트용 메타 애노테이션이다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Import({TestcontainerConfig.class, IntegrationTestConfig.class, AcceptanceTestConfiguration.class})
@TestExecutionListeners(
        listeners = IntegrationTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}

package me.bombom.support.acceptance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.bombom.support.IntegrationTest;
import org.springframework.test.context.TestExecutionListeners;

@IntegrationTest
@Target(ElementType.TYPE)
@TestExecutionListeners(
        listeners = AcceptanceTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptanceTest {

    String value();
}

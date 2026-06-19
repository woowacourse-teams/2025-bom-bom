package me.bombom.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Clock;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Target(ElementType.TYPE)
@Import(TestcontainerConfig.class)
@MockitoBean(types = {Clock.class, OAuth2LoginSuccessHandler.class}, enforceOverride = true)
@TestExecutionListeners(
        listeners = DefaultClockMockTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}

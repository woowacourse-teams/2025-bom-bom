package me.bombom.support;

import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DefaultClockMockTestExecutionListener extends AbstractTestExecutionListener {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock SYSTEM_CLOCK = Clock.system(SEOUL_ZONE);

    @Override
    public void beforeTestMethod(TestContext testContext) {
        Clock clock = testContext.getApplicationContext().getBean(Clock.class);

        given(clock.instant()).willAnswer(invocation -> SYSTEM_CLOCK.instant());
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}

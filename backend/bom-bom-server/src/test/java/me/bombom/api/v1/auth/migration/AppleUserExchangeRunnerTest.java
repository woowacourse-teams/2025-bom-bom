package me.bombom.api.v1.auth.migration;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class AppleUserExchangeRunnerTest {

    private final AppleUserExchangeService service = mock(AppleUserExchangeService.class);

    @Test
    void execute가_false이면_미리보기만_수행한다() throws Exception {
        AppleUserExchangeProperties properties = new AppleUserExchangeProperties(
                "A1B2C3D4E5", "KEY123ID45", "com.example.app", "dummy-pem", false);
        given(service.preview()).willReturn(5L);

        new AppleUserExchangeRunner(properties, service).run(mock(ApplicationArguments.class));

        verify(service).preview();
        verify(service, never()).exchangeAll();
    }

    @Test
    void execute가_true이면_검증후_배치를_수행한다() throws Exception {
        AppleUserExchangeProperties properties = new AppleUserExchangeProperties(
                "A1B2C3D4E5", "KEY123ID45", "com.example.app", "dummy-pem", true);
        given(service.exchangeAll()).willReturn(new AppleUserExchangeResult(5L, 5L, 0L));

        new AppleUserExchangeRunner(properties, service).run(mock(ApplicationArguments.class));

        verify(service).exchangeAll();
    }
}

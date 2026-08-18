package me.bombom.api.v1.auth.migration;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class AppleUserMigrationRunnerTest {

    private final AppleUserMigrationService service = mock(AppleUserMigrationService.class);

    @Test
    void execute가_false이면_미리보기만_수행한다() throws Exception {
        AppleUserMigrationProperties properties = new AppleUserMigrationProperties("A1B2C3D4E5", "com.example.app", false);
        given(service.preview()).willReturn(12L);

        new AppleUserMigrationRunner(properties, service).run(mock(ApplicationArguments.class));

        verify(service).preview();
        verify(service, never()).migrateAll();
    }

    @Test
    void execute가_true이면_검증후_배치를_수행한다() throws Exception {
        AppleUserMigrationProperties properties = new AppleUserMigrationProperties("A1B2C3D4E5", "com.example.app", true);
        given(service.migrateAll()).willReturn(new AppleUserMigrationResult(12L, 12L, 0L));

        new AppleUserMigrationRunner(properties, service).run(mock(ApplicationArguments.class));

        verify(service).migrateAll();
    }
}

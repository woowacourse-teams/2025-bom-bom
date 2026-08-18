package me.bombom.api.v1.auth.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("apple-user-migration")
@RequiredArgsConstructor
public class AppleUserMigrationRunner implements ApplicationRunner {

    private final AppleUserMigrationProperties properties;
    private final AppleUserMigrationService service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Apple 사용자 이전 설정 - clientId: {}, targetTeamId: {}, execute: {}",
                properties.clientId(), properties.targetTeamId(), properties.execute());

        if (!properties.execute()) {
            log.info("Apple 사용자 이전 dry-run - 대상 수: {}", service.preview());
            return;
        }

        properties.validateForExecution();
        AppleUserMigrationResult result = service.migrateAll();
        log.info("Apple 사용자 이전 완료 - 대상 수: {}, 저장 수: {}, 실패 수: {}",
                result.targetCount(), result.migratedCount(), result.failedCount());
    }
}

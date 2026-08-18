package me.bombom.api.v1.auth.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("apple-user-exchange")
@RequiredArgsConstructor
public class AppleUserExchangeRunner implements ApplicationRunner {

    private final AppleUserExchangeProperties properties;
    private final AppleUserExchangeService service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Apple 새 팀 sub 교환 설정 - targetTeamId: {}, execute: {}",
                properties.teamId(), properties.execute());

        if (!properties.execute()) {
            log.info("Apple 새 팀 sub 교환 dry-run - 대상 수: {}", service.preview());
            return;
        }

        properties.validateForExecution();
        AppleUserExchangeResult result = service.exchangeAll();
        log.info("Apple 새 팀 sub 교환 완료 - 대상 수: {}, 저장 수: {}, 실패 수: {}",
                result.targetCount(), result.migratedCount(), result.failedCount());
    }
}

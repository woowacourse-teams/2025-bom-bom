package me.bombom.api.v1.auth.migration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties("oauth2.apple.migration")
public record AppleUserMigrationProperties(
        String targetTeamId,
        String clientId,
        boolean execute
) {

    private static final String TEAM_ID_PATTERN = "[A-Z0-9]{10}";

    public void validateForExecution() {
        if (!execute) {
            throw new IllegalStateException("Apple 사용자 이전 실행 플래그가 활성화되지 않았습니다.");
        }
        if (targetTeamId == null || !targetTeamId.matches(TEAM_ID_PATTERN)) {
            throw new IllegalStateException("Apple 사용자 이전 수신 Team ID가 올바르지 않습니다.");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("Apple 사용자 이전 client ID가 필요합니다.");
        }
    }
}

package me.bombom.api.v1.auth.migration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties("oauth2.apple.exchange")
public record AppleUserExchangeProperties(
        String teamId,
        String keyId,
        String clientId,
        String privateKey,
        boolean execute
) {

    private static final String TEAM_ID_PATTERN = "[A-Z0-9]{10}";

    public void validateForExecution() {
        if (!execute) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 실행 플래그가 활성화되지 않았습니다.");
        }
        if (teamId == null || !teamId.matches(TEAM_ID_PATTERN)) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 대상 Team ID가 올바르지 않습니다.");
        }
        if (!StringUtils.hasText(keyId)) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 Key ID가 필요합니다.");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 client ID가 필요합니다.");
        }
        if (!StringUtils.hasText(privateKey)) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 private key가 필요합니다.");
        }
    }
}

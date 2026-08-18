package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AppleUserExchangePropertiesTest {

    private static final String VALID_PEM = "-----BEGIN PRIVATE KEY-----\ndummy\n-----END PRIVATE KEY-----";

    @Test
    void 유효하지_않은_Team_ID면_실행을_거부한다() {
        AppleUserExchangeProperties properties =
                new AppleUserExchangeProperties("invalid", "key_id", "com.example.app", VALID_PEM, true);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void execute가_false이면_실행을_거부한다() {
        AppleUserExchangeProperties properties =
                new AppleUserExchangeProperties("A1B2C3D4E5", "key_id", "com.example.app", VALID_PEM, false);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void privateKey가_없으면_실행을_거부한다() {
        AppleUserExchangeProperties properties =
                new AppleUserExchangeProperties("A1B2C3D4E5", "key_id", "com.example.app", null, true);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keyId가_없으면_실행을_거부한다() {
        AppleUserExchangeProperties properties =
                new AppleUserExchangeProperties("A1B2C3D4E5", null, "com.example.app", VALID_PEM, true);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }
}

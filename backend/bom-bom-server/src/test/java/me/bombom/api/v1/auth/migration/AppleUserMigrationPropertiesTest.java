package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AppleUserMigrationPropertiesTest {

    @Test
    void 유효하지_않은_수신_Team_ID면_실행을_거부한다() {
        AppleUserMigrationProperties properties = new AppleUserMigrationProperties("invalid", "com.example.app", true);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void execute가_false이면_실행을_거부한다() {
        AppleUserMigrationProperties properties = new AppleUserMigrationProperties("A1B2C3D4E5", "com.example.app", false);

        assertThatThrownBy(properties::validateForExecution)
                .isInstanceOf(IllegalStateException.class);
    }
}

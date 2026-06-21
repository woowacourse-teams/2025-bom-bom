package me.bombom.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CleanUpTest {

    @Autowired
    private CleanUp cleanUp;

    @Test
    void 정리_대상에_도메인_테이블이_포함된다() {
        // when
        List<String> tables = cleanUp.cleanableTableNames();

        // then
        assertThat(tables).contains("member", "challenge");
    }

    @Test
    void 정리_대상에서_seed와_인프라_테이블은_제외된다() {
        // when
        List<String> tables = cleanUp.cleanableTableNames();

        // then
        assertThat(tables)
                .doesNotContain("flyway_schema_history")
                .doesNotContain("SPRING_SESSION")
                .doesNotContain("holiday")
                .doesNotContain("monthly_reading_snapshot_meta");
    }
}

package me.bombom.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class CleanUpTest {

    @Autowired
    private CleanUp cleanUp;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        // flyway_schema_history, SPRING_SESSION은 JPA 엔티티가 아니라 메타모델에 없으므로 자동 제외된다.
        assertThat(tables)
                .doesNotContain("flyway_schema_history")
                .doesNotContain("SPRING_SESSION")
                .contains("reading_snapshot_meta")
                .doesNotContain("unsubscribe_pattern");
    }

    @Test
    void all_호출시_도메인_데이터는_지우고_변경된_seed는_기준값으로_복원한다() {
        // given
        memberRepository.save(TestFixture.uniqueMemberFixture());
        jdbcTemplate.update("UPDATE reading_snapshot_meta SET snapshot_at = '2099-12-31 23:59:59.999999'");

        // when
        cleanUp.all();

        // then
        assertThat(memberRepository.count()).isZero();
        assertThat(readingSnapshotMeta()).containsOnly(
                new SnapshotMeta("MONTHLY", LocalDateTime.of(2000, 1, 1, 0, 0)),
                new SnapshotMeta("CONTINUE", LocalDateTime.of(2000, 1, 1, 0, 0))
        );
    }

    private List<SnapshotMeta> readingSnapshotMeta() {
        return jdbcTemplate.query(
                "SELECT snapshot_type, snapshot_at FROM reading_snapshot_meta",
                (resultSet, rowNumber) -> new SnapshotMeta(
                        resultSet.getString("snapshot_type"),
                        resultSet.getTimestamp("snapshot_at").toLocalDateTime()
                )
        );
    }

    private record SnapshotMeta(
            String snapshotType,
            LocalDateTime snapshotAt
    ) {
    }
}

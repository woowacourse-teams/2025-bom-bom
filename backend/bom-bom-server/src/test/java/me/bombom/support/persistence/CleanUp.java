package me.bombom.support.persistence;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.MappingMetamodel;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 통합 테스트 사이에 JPA 도메인 테이블을 비우고 필요한 seed 데이터를 기준값으로 복원한다.
 */
public class CleanUp {

    private static final String READING_SNAPSHOT_META_SEED_SQL = """
            INSERT INTO reading_snapshot_meta (snapshot_type, snapshot_at)
            VALUES ('MONTHLY', '2000-01-01 00:00:00.000000'),
                   ('CONTINUE', '2000-01-01 00:00:00.000000')
            """;

    /**
     * 읽기 전용 seed 테이블. 무조건적인 Flyway seed가 존재하고 활성(@Disabled가 아닌) 테스트가
     * INSERT/DELETE로 변경하지 않는 테이블만 제외한다.
     * - unsubscribe_pattern: 유일한 writer인 UnsubscribeAgentTest가 @Disabled이므로 정적 참조 seed로 취급한다.
     */
    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "unsubscribe_pattern"
    );

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    private List<String> cleanableTableNames;

    public CleanUp(
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate) {
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void all() {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (var statement = connection.createStatement()) {
                for (String table : cleanableTableNames()) {
                    statement.executeUpdate("DELETE FROM `" + table + "`");
                }
                statement.executeUpdate(READING_SNAPSHOT_META_SEED_SQL);
            }
            return null;
        });
    }

    public List<String> cleanableTableNames() {
        if (cleanableTableNames == null) {
            cleanableTableNames = resolveCleanableTableNames();
        }
        return cleanableTableNames;
    }

    private List<String> resolveCleanableTableNames() {
        MappingMetamodel mappingMetamodel = resolveMappingMetamodel();

        // SINGLE_TABLE 상속 계층은 같은 물리 테이블명을 여러 번 반환할 수 있으므로 LinkedHashSet으로 중복을 제거한다.
        Set<String> uniqueTableNames = new LinkedHashSet<>();
        mappingMetamodel.forEachEntityDescriptor(descriptor -> {
            String tableName = descriptor.getMappedTableDetails().getTableName().replace("`", "");
            if (!EXCLUDED_TABLES.contains(tableName)) {
                uniqueTableNames.add(tableName);
            }
        });
        return new ArrayList<>(uniqueTableNames);
    }

    private MappingMetamodel resolveMappingMetamodel() {
        return entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class)
                .getMappingMetamodel();
    }
}

package me.bombom.support;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.MappingMetamodel;
import org.springframework.jdbc.core.JdbcTemplate;

public class CleanUp {

    /**
     * 읽기 전용 seed 테이블. Flyway 적용 직후 행이 있고 테스트가 직접 변경하지 않는 테이블이다.
     * 빈 테스트 DB에 마이그레이션을 적용해 비어있지 않은 테이블 중,
     * 현재 테스트가 deleteAllInBatch로 지우지 않는 것을 골라 구성했다.
     */
    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "monthly_reading_snapshot_meta",
            "continue_reading_snapshot_meta",
            "reading_snapshot_meta",
            "holiday",
            "unsubscribe_pattern",
            "newsletter_previous_policy",
            "badge"
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

    public List<String> cleanableTableNames() {
        if (cleanableTableNames == null) {
            cleanableTableNames = resolveCleanableTableNames();
        }
        return cleanableTableNames;
    }

    private List<String> resolveCleanableTableNames() {
        MappingMetamodel mappingMetamodel = resolveMappingMetamodel();

        List<String> tableNames = new ArrayList<>();
        mappingMetamodel.forEachEntityDescriptor(descriptor -> {
            String rawTableName = descriptor.getMappedTableDetails().getTableName();
            String tableName = rawTableName.replace("`", "");
            if (!EXCLUDED_TABLES.contains(tableName)) {
                tableNames.add(tableName);
            }
        });
        return tableNames;
    }

    private MappingMetamodel resolveMappingMetamodel() {
        return entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class)
                .getMappingMetamodel();
    }
}

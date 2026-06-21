package me.bombom.support;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    public void all() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (String table : cleanableTableNames()) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
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

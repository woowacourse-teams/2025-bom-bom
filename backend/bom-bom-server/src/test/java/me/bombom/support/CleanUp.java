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
     * 읽기 전용 seed 테이블. 무조건적인 Flyway seed가 존재하고 활성(@Disabled가 아닌) 테스트가
     * INSERT/DELETE로 변경하지 않는 테이블만 제외한다.
     * - reading_snapshot_meta: 앱 랭킹 로직이 seed된 행(MONTHLY/CONTINUE)을 읽으므로 보존한다.
     * - unsubscribe_pattern: 유일한 writer인 UnsubscribeAgentTest가 @Disabled이므로 정적 참조 seed로 취급한다.
     */
    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "reading_snapshot_meta",
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

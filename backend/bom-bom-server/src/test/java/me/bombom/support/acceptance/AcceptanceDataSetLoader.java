package me.bombom.support.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인수 테스트용 JSON 데이터셋을 읽어 테이블별로 삭제, 삽입, 추가 적재를 수행한다.
 */
public class AcceptanceDataSetLoader {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final String CURRENT_DATE_TIME = "${CURRENT_DATE_TIME}";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Clock clock;

    public AcceptanceDataSetLoader(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.clock = clock;
    }

    @Transactional
    public void load(String... classpathLocations) {
        Map<String, List<JsonNode>> rowsByTable = mergeDataSets(classpathLocations);
        deleteExistingRows(new ArrayList<>(rowsByTable.keySet()));
        insertRows(rowsByTable);
    }

    @Transactional
    public void append(String... classpathLocations) {
        insertRows(mergeDataSets(classpathLocations));
    }

    @Transactional
    public void clear(String... classpathLocations) {
        Map<String, List<JsonNode>> rowsByTable = mergeDataSets(classpathLocations);
        deleteExistingRows(new ArrayList<>(rowsByTable.keySet()));
    }

    private Map<String, List<JsonNode>> mergeDataSets(String[] classpathLocations) {
        if (classpathLocations.length == 0) {
            throw new IllegalArgumentException("인수 테스트 데이터셋 경로가 하나 이상 필요합니다.");
        }

        Map<String, List<JsonNode>> rowsByTable = new LinkedHashMap<>();
        for (String classpathLocation : classpathLocations) {
            JsonNode dataSet = readDataSet(classpathLocation);
            tables(dataSet).forEach(table -> {
                List<JsonNode> rows = rowsByTable.computeIfAbsent(table.getKey(), ignored -> new ArrayList<>());
                table.getValue().forEach(rows::add);
            });
        }
        return rowsByTable;
    }

    private JsonNode readDataSet(String classpathLocation) {
        Resource resource = resourceLoader.getResource("classpath:" + classpathLocation);
        if (!resource.exists()) {
            throw new IllegalArgumentException("인수 테스트 데이터셋을 찾을 수 없습니다: " + classpathLocation);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode dataSet = objectMapper.readTree(inputStream);
            if (dataSet == null || !dataSet.isObject()) {
                throw new IllegalArgumentException("데이터셋의 최상위 값은 객체여야 합니다: " + classpathLocation);
            }
            return dataSet;
        } catch (IOException exception) {
            throw new IllegalStateException("인수 테스트 데이터셋을 읽지 못했습니다: " + classpathLocation, exception);
        }
    }

    private List<Map.Entry<String, JsonNode>> tables(JsonNode dataSet) {
        List<Map.Entry<String, JsonNode>> tables = new ArrayList<>();
        dataSet.properties().forEach(entry -> {
            validateIdentifier(entry.getKey());
            if (!entry.getValue().isArray()) {
                throw new IllegalArgumentException("테이블 데이터는 배열이어야 합니다: " + entry.getKey());
            }
            tables.add(entry);
        });
        return tables;
    }

    private void deleteExistingRows(List<String> tables) {
        for (int index = tables.size() - 1; index >= 0; index--) {
            String table = tables.get(index);
            jdbcTemplate.execute("DELETE FROM " + quote(table));
        }
    }

    private void insertRows(Map<String, List<JsonNode>> rowsByTable) {
        rowsByTable.forEach(this::insertRows);
    }

    private void insertRows(String table, List<JsonNode> rows) {
        Map<List<String>, List<JsonNode>> rowsByColumns = new LinkedHashMap<>();
        for (JsonNode row : rows) {
            List<String> columns = columns(table, row);
            rowsByColumns.computeIfAbsent(columns, ignored -> new ArrayList<>()).add(row);
        }

        rowsByColumns.forEach((columns, groupedRows) -> batchInsert(table, columns, groupedRows));
    }

    private List<String> columns(String table, JsonNode row) {
        if (!row.isObject() || row.isEmpty()) {
            throw new IllegalArgumentException("행 데이터는 비어 있지 않은 객체여야 합니다: " + table);
        }

        List<String> columns = new ArrayList<>();
        row.properties().forEach(field -> {
            validateIdentifier(field.getKey());
            columns.add(field.getKey());
        });
        return List.copyOf(columns);
    }

    private void batchInsert(String table, List<String> columns, List<JsonNode> rows) {
        String columnSql = columns.stream()
                .map(this::quote)
                .reduce((left, right) -> left + ", " + right)
                .orElseThrow();
        String placeholderSql = String.join(", ", Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO " + quote(table) + " (" + columnSql + ") VALUES (" + placeholderSql + ")";

        jdbcTemplate.batchUpdate(
                sql,
                rows,
                rows.size(),
                (preparedStatement, row) -> setValues(preparedStatement, values(row, columns))
        );
    }

    private List<JsonNode> values(JsonNode row, List<String> columns) {
        return columns.stream().map(row::get).toList();
    }

    private void setValues(PreparedStatement preparedStatement, List<JsonNode> values) throws SQLException {
        for (int index = 0; index < values.size(); index++) {
            JsonNode value = values.get(index);
            int parameterIndex = index + 1;

            if (value.isNull()) {
                preparedStatement.setObject(parameterIndex, null);
            } else if (value.isTextual() && CURRENT_DATE_TIME.equals(value.textValue())) {
                preparedStatement.setTimestamp(parameterIndex, Timestamp.valueOf(LocalDateTime.now(clock)));
            } else if (value.isBoolean()) {
                preparedStatement.setBoolean(parameterIndex, value.booleanValue());
            } else if (value.isIntegralNumber()) {
                preparedStatement.setLong(parameterIndex, value.longValue());
            } else if (value.isFloatingPointNumber()) {
                preparedStatement.setDouble(parameterIndex, value.doubleValue());
            } else {
                preparedStatement.setString(parameterIndex, value.asText());
            }
        }
    }

    private void validateIdentifier(String identifier) {
        if (!SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("허용되지 않은 SQL 식별자입니다: " + identifier);
        }
    }

    private String quote(String identifier) {
        return "`" + identifier + "`";
    }
}

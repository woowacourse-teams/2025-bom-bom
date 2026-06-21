package me.bombom.support.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

public class AcceptanceDataSetLoader {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public AcceptanceDataSetLoader(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public void load(String classpathLocation) {
        JsonNode dataSet = readDataSet(classpathLocation);
        List<Map.Entry<String, JsonNode>> tables = tables(dataSet);

        deleteExistingRows(tables);
        insertRows(tables);
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

    private void deleteExistingRows(List<Map.Entry<String, JsonNode>> tables) {
        for (int index = tables.size() - 1; index >= 0; index--) {
            String table = tables.get(index).getKey();
            jdbcTemplate.execute("DELETE FROM " + quote(table));
        }
    }

    private void insertRows(List<Map.Entry<String, JsonNode>> tables) {
        for (Map.Entry<String, JsonNode> table : tables) {
            insertRows(table.getKey(), table.getValue());
        }
    }

    private void insertRows(String table, JsonNode rows) {
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

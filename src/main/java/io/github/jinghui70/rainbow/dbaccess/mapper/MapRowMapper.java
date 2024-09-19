package io.github.jinghui70.rainbow.dbaccess.mapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.h2.value.CaseInsensitiveMap;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link org.springframework.jdbc.core.RowMapper} implementation that creates a {@link Map}
 * for each row, representing all columns as key-value pairs: one entry for each column,
 * with the column name as key.
 *
 * <p>This RowMapper is useful when you want to access all columns of a row without
 * having to define a class for it.
 *
 * <p>This class is thread-safe.
 *
 * @author jinghui70
 */
public class MapRowMapper extends ColumnMapRowMapper {

    public static MapRowMapper INSTANCE = new MapRowMapper();

    private Map<String, FieldMapper<?>> mapperMap;

    private boolean ignoreNull;

    private Set<String> ignoreKeySet;


    /**
     * Ignore the specified keys when mapping.
     * <p>
     * If the column name of the query result is in the ignoreKeySet, the key will be ignored.
     * <p>
     * Note: The ignoreKeySet is case-insensitive.
     *
     * @param ignoreKeys the keys to be ignored
     * @return this
     */
    public MapRowMapper ignore(String... ignoreKeys) {
        if (ignoreKeySet == null) {
            ignoreKeySet = new HashSet<>();
        }
        for (String str : ignoreKeys)
            ignoreKeySet.add(str.toUpperCase());
        return this;
    }

    /**
     * If the value of the column is null, the key will not be put into the Map.
     * <p>
     * Default is false, that means the key will be put into the Map even if the value is null.
     * <p>
     * Note: If the value is null and ignoreNull is true, the key will not be put into the Map.
     * So you can use this method to avoid the key of null value.
     *
     * @return this
     */
    public MapRowMapper ignoreNull() {
        ignoreNull = true;
        return this;
    }

    /**
     * Set the FieldMapper of specified key.
     *
     * @param key         the key of the FieldMapper
     * @param fieldMapper the FieldMapper
     * @return this
     */
    public MapRowMapper setFieldMapper(String key, FieldMapper<?> fieldMapper) {
        if (mapperMap == null)
            mapperMap = new CaseInsensitiveMap<>();
        mapperMap.put(key, fieldMapper);
        return this;
    }

    /**
     * Set the FieldMapper of specified column index.
     *
     * @param columnIndex the column index of the FieldMapper
     * @param fieldMapper the FieldMapper
     * @return this
     */
    public MapRowMapper setFieldMapper(int columnIndex, FieldMapper<?> fieldMapper) {
        return this.setFieldMapper(Integer.toString(columnIndex), fieldMapper);
    }

    /**
     * Implement the RowMapper interface, mapping the column values to a Map.
     * The key of the Map is the column name, and the value is the column value.
     * If the column value is null and ignoreNull is true, the key will not be put into the Map.
     * If ignoreKeySet is not null, the column names in the set will be ignored.
     *
     * @param rs     the ResultSet to map
     * @param rowNum the row number (not used)
     * @return the mapped Map
     * @throws SQLException if there is a problem with the underlying database
     */
    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> mapOfColumnValues = createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String column = JdbcUtils.lookupColumnName(metaData, i);
            if (ignoreKeySet != null && ignoreKeySet.contains(column.toUpperCase())) continue;
            String key = getColumnKey(column);
            FieldMapper<?> mapper = getFieldMapper(i, column);
            Object value = mapper != null ? mapper.formDB(rs, i) : getColumnValue(rs, i);
            if (value != null || !ignoreNull)
                mapOfColumnValues.putIfAbsent(key, value);
        }
        return mapOfColumnValues;
    }

    /**
     * Get the FieldMapper of specified column index or column name.
     *
     * <p>First try to get the FieldMapper from the mapperMap by the column index.
     * If not found, try to get the FieldMapper from the mapperMap by the column name.
     *
     * @param columnIndex the column index
     * @param column      the column name
     * @return the FieldMapper or null if not found
     */
    private FieldMapper<?> getFieldMapper(int columnIndex, String column) {
        if (mapperMap == null) return null;
        FieldMapper<?> mapper = mapperMap.get(Integer.toString(columnIndex));
        return mapper == null ? mapperMap.get(column) : mapper;
    }

    /**
     * Create a new instance of MapRowMapper.
     *
     * @return a new MapRowMapper
     */
    public static MapRowMapper create() {
        return new MapRowMapper();
    }

}

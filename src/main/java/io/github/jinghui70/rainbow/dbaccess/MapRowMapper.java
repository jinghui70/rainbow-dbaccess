package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MapRowMapper implements RowMapper<Map<String, Object>> {

    public static MapRowMapper INSTANCE = new MapRowMapper();

    // 指定key改名
    private Map<String, String> keyMappingMap;

    // 值加工
    private Map<String, Function<?,?>> transformMap;

    // 后处理
    private Consumer<Map<String, Object>> postConsumer;

    // 忽略空
    private boolean ignoreNull;

    private KeyType keyType = KeyType.UPPER_CASE;

    public MapRowMapper setKeyType(KeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * 字段改名配置
     *
     * @param oldKey 读出来的字段名
     * @param newKey 写入Map的字段名
     * @return 自己
     */
    public MapRowMapper rename(String oldKey, String newKey) {
        if (keyMappingMap == null)
            keyMappingMap = new LinkedCaseInsensitiveMap<>();
        keyMappingMap.put(oldKey, newKey);
        return this;
    }

    /**
     * 忽略某些字段
     *
     * @param ignoreKeys 忽略的字段名
     * @return 自己
     */
    public MapRowMapper ignore(String... ignoreKeys) {
        for (String ignoreKey : ignoreKeys) {
            rename(ignoreKey, null);
        }
        return this;
    }

    /**
     * 忽略空值
     *
     * @return 自己
     */
    public MapRowMapper ignoreNull() {
        ignoreNull = true;
        return this;
    }

    /**
     * 对某个字段结果进行加工的函数
     *
     * @param key      字段名
     * @param function 加工函数
     * @return 自己
     */
    public MapRowMapper transform(String key, Function<?, ?> function) {
        if (transformMap == null)
            transformMap = new LinkedCaseInsensitiveMap<>();
        transformMap.put(key, function);
        return this;
    }

    /**
     * 记录读取之后的后处理
     *
     * @param consumer 处理函数
     * @return 自己
     */
    public MapRowMapper post(Consumer<Map<String, Object>> consumer) {
        postConsumer = consumer;
        return this;
    }

    protected String getColumnKey(String columnName) {
        columnName = keyType.apply(columnName);
        if (keyMappingMap != null) {
            return keyMappingMap.getOrDefault(columnName, columnName);
        }
        return columnName;
    }

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> mapOfColValues = createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String key = getColumnKey(JdbcUtils.lookupColumnName(metaData, i));
            if (key != null) {
                Object value = getColumnValue(rs, i);
                value = transform(key, value);
                if (value != null || !ignoreNull)
                    mapOfColValues.put(key, value);
            }
        }
        if (postConsumer != null)
            postConsumer.accept(mapOfColValues);
        return mapOfColValues;
    }

    protected Map<String, Object> createColumnMap(int columnCount) {
        return new LinkedHashMap<>(columnCount);
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }

    protected <F> Object transform(String key, F value) {
        if (transformMap != null) {
            @SuppressWarnings("unchecked")
            Function<F,?> decorator = (Function<F,?>) transformMap.get(key);
            if (decorator != null)
                return decorator.apply(value);
        }
        return value;
    }

    public static MapRowMapper create() {
        return new MapRowMapper();
    }

}

package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.h2.value.CaseInsensitiveMap;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * 将数据库行映射为Object数组的RowMapper。
 * 支持为指定列或列索引设置自定义FieldMapper。
 */
public class ObjectArrayRowMapper implements RowMapper<Object[]> {

    private Map<String, FieldMapper<?>> mapperMap;

    /**
     * 为指定列索引设置FieldMapper。
     *
     * @param columnIndex 列索引（从1开始）
     * @param fieldMapper 字段映射器
     * @return 当前ObjectArrayRowMapper实例
     */
    public ObjectArrayRowMapper setFieldMapper(int columnIndex, FieldMapper<?> fieldMapper) {
        return this.setFieldMapper(Integer.toString(columnIndex), fieldMapper);
    }

    /**
     * 为指定列名设置FieldMapper。
     *
     * @param key 列名
     * @param fieldMapper 字段映射器
     * @return 当前ObjectArrayRowMapper实例
     */
    public ObjectArrayRowMapper setFieldMapper(String key, FieldMapper<?> fieldMapper) {
        if (mapperMap == null)
            mapperMap = new CaseInsensitiveMap<>();
        mapperMap.put(key, fieldMapper);
        return this;
    }

    /**
     * 将ResultSet的当前行映射为Object数组。
     *
     * @param rs ResultSet对象
     * @param rowNum 行号
     * @return 包含列值的Object数组
     * @throws SQLException 如果发生SQL异常
     */
    @Override
    public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Object[] result = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            String column = JdbcUtils.lookupColumnName(metaData, i);
            FieldMapper<?> mapper = getFieldMapper(i, column);
            result[i - 1] = mapper != null ? mapper.formDB(rs, i) : JdbcUtils.getResultSetValue(rs, i);
        }
        return result;
    }

    /**
     * 获取指定列的FieldMapper。
     * 先按列索引查找，未找到则按列名查找。
     *
     * @param columnIndex 列索引
     * @param column 列名
     * @return FieldMapper实例，如果未找到则返回null
     */
    private FieldMapper<?> getFieldMapper(int columnIndex, String column) {
        if (mapperMap == null) return null;
        FieldMapper<?> mapper = mapperMap.get(Integer.toString(columnIndex));
        return mapper == null ? mapperMap.get(column) : mapper;
    }
}

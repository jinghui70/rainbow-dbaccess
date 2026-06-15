package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.h2.value.CaseInsensitiveMap;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class ObjectArrayRowMapper implements RowMapper<Object[]> {

    private Map<String, FieldMapper<?>> mapperMap;

    public ObjectArrayRowMapper setFieldMapper(int columnIndex, FieldMapper<?> fieldMapper) {
        return this.setFieldMapper(Integer.toString(columnIndex), fieldMapper);
    }

    public ObjectArrayRowMapper setFieldMapper(String key, FieldMapper<?> fieldMapper) {
        if (mapperMap == null)
            mapperMap = new CaseInsensitiveMap<>();
        mapperMap.put(key, fieldMapper);
        return this;
    }

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

    private FieldMapper<?> getFieldMapper(int columnIndex, String column) {
        if (mapperMap == null) return null;
        FieldMapper<?> mapper = mapperMap.get(Integer.toString(columnIndex));
        return mapper == null ? mapperMap.get(column) : mapper;
    }
}

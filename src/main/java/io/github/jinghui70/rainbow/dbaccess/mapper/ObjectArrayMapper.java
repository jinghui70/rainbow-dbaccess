package io.github.jinghui70.rainbow.dbaccess.mapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ObjectArrayMapper implements RowMapper<Object[]> {

    private Map<Integer, FieldMapper<?>> mapperMap;

    public ObjectArrayMapper setFieldMapper(int index, FieldMapper<?> mapper) {
        if (mapperMap == null) mapperMap = new HashMap<>();
        mapperMap.put(index, mapper);
        return this;
    }

    @Override
    public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Object[] result = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            FieldMapper<?> mapper = mapperMap == null ? null : mapperMap.get(i);
            result[i - 1] = mapper != null ? mapper.formDB(rs, i) : JdbcUtils.getResultSetValue(rs, i);
        }
        return result;
    }

}

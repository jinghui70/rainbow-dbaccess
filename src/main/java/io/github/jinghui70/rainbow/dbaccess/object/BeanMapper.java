package io.github.jinghui70.rainbow.dbaccess.object;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * 支持数组属性的对象RowMapper
 *
 * @param <T> 对象泛型
 */
public class BeanMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Map<String, PropInfo> propMap;

    private BeanMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.propMap = PropInfo.getPropInfoMap(mappedClass);
    }

    public static <T> BeanMapper<T> of(Class<T> clazz) {
        return new BeanMapper<>(clazz);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T result = BeanUtils.instantiateClass(this.mappedClass);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(metaData, index);
            String field = StringUtils.delete(column, " ").toLowerCase();
            PropInfo propInfo = propMap.get(field);
            if (propInfo != null) {
                Object value = propInfo.getValue(rs, index);
                propInfo.setValue(result, value);
            }
        }
        return result;
    }

}

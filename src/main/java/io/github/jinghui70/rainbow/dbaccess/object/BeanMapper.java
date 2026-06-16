package io.github.jinghui70.rainbow.dbaccess.object;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支持数组属性的对象RowMapper
 *
 * @param <T> 对象泛型
 */
public class BeanMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Map<String, PropInfo> propMap;

    /**
     * 私有构造函数。
     *
     * @param mappedClass 目标映射类
     */
    private BeanMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.propMap = PropInfoCache.get(mappedClass);
    }

    /**
     * 构造函数，使用指定的属性映射。
     *
     * @param mappedClass 目标映射类
     * @param propMap 属性信息Map
     */
    BeanMapper(Class<T> mappedClass, LinkedHashMap<String, PropInfo> propMap) {
        this.mappedClass = mappedClass;
        this.propMap = propMap;
    }

    /**
     * 创建BeanMapper实例。
     *
     * @param clazz 目标映射类
     * @param <T> 目标类型
     * @return BeanMapper实例
     */
    public static <T> BeanMapper<T> of(Class<T> clazz) {
        return new BeanMapper<>(clazz);
    }

    /**
     * 将ResultSet的当前行映射为目标对象。
     *
     * @param rs ResultSet对象
     * @param rowNum 行号
     * @return 映射后的对象
     * @throws SQLException 如果发生SQL异常
     */
    @Override
    @NonNull
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

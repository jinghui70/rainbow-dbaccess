package com.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import com.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import com.github.jinghui70.rainbow.dbaccess.annotation.Column;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持数组属性的对象RowMapper
 *
 * @param <T>
 * @author lijinghui
 */
public class BeanMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Map<String, Object> propMap = new CaseInsensitiveMap<>();
    private Map<PropDesc, Integer> arrayProp;

    public BeanMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        BeanDesc desc = BeanUtil.getBeanDesc(mappedClass);
        for (PropDesc prop : desc.getProps()) {
            ArrayField a = prop.getField().getAnnotation(ArrayField.class);
            Column fieldAnnotation = prop.getField().getAnnotation(Column.class);
            String key = fieldAnnotation == null ? StrUtil.toUnderlineCase(prop.getRawFieldName()) : fieldAnnotation.name();
            if (a == null) {
                propMap.put(key, prop);
            } else {
                if (arrayProp == null)
                    arrayProp = new HashMap<>();
                arrayProp.put(prop, a.length());
                String join = a.underline() ? "_" : "";
                for (int i = 0; i < a.length(); i++) {
                    String field = String.format("%s%s%d", key, join, i + a.start());
                    propMap.put(field, new ArrayProp(prop, i));
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T result = BeanUtils.instantiateClass(this.mappedClass);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(metaData, index);
            String field = StringUtils.delete(column, " ").toLowerCase();
            Object prop = propMap.get(field);
            if (prop != null) {
                if (prop instanceof PropDesc) {
                    PropDesc p = (PropDesc) prop;
                    Object value = JdbcUtils.getResultSetValue(rs, index, p.getFieldClass());
                    p.setValue(result, value);
                } else {
                    ArrayProp ap = (BeanMapper.ArrayProp) prop;
                    Class<?> type = ap.prop.getFieldClass().getComponentType();
                    Object array = ap.prop.getValue(result);
                    if (array == null) {
                        array = Array.newInstance(type, arrayProp.get(ap.prop));
                        ap.prop.setValue(result, array);
                    }
                    Object value = JdbcUtils.getResultSetValue(rs, index, type);
                    if (value != null)
                        Array.set(array, ap.index, value);
                }
            }
        }
        return result;
    }

    private static class ArrayProp {
        PropDesc prop;
        int index;

        ArrayProp(PropDesc prop, int index) {
            this.prop = prop;
            this.index = index;
        }
    }

}

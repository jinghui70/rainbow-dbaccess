package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
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
import java.util.function.Function;

/**
 * 支持数组属性的对象RowMapper
 *
 * @param <T> 对象泛型
 */
public class BeanMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Map<String, Object> propMap = new CaseInsensitiveMap<>();
    private Map<PropDesc, Integer> arrayProp;
    private Map<String, Function<String, ?>> transformMap;

    private BeanMapperPostProcessor<T> postProcessor;

    private BeanMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        BeanDesc desc = BeanUtil.getBeanDesc(mappedClass);
        for (PropDesc prop : desc.getProps()) {
            ArrayField a = prop.getField().getAnnotation(ArrayField.class);
            String fieldName = prop.getRawFieldName();
            Column column = prop.getField().getAnnotation(Column.class);
            fieldName = column == null || StrUtil.isEmpty(column.name()) ? StrUtil.toUnderlineCase(fieldName) : column.name();
            if (a == null) {
                propMap.put(fieldName, prop);
            } else {
                if (arrayProp == null)
                    arrayProp = new HashMap<>();
                arrayProp.put(prop, a.length());
                String join = a.underline() ? "_" : "";
                for (int i = 0; i < a.length(); i++) {
                    String field = String.format("%s%s%d", fieldName, join, i + a.start());
                    propMap.put(field, new ArrayProp(prop, i));
                }
            }
        }
    }

    public static <T> BeanMapper<T> of(Class<T> clazz) {
        return new BeanMapper<>(clazz);
    }

    public BeanMapper<T> decode(String field, Function<String, ?> function) {
        if (transformMap == null)
            transformMap = new HashMap<>();
        transformMap.put(field, function);
        return this;
    }

    /**
     * 全部字段处理完之后的后处理，比如有些字段需要组合成一个属性
     *
     * @param postProcessor 后处理函数
     * @return 自己
     */
    public BeanMapper<T> post(BeanMapperPostProcessor<T> postProcessor) {
        this.postProcessor = postProcessor;
        return this;
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
                    Object value = getPropValue(rs, index, p);
                    p.setValue(result, value);
                } else {
                    ArrayProp ap = (ArrayProp) prop;
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
        if (postProcessor != null) {
            postProcessor.process(result, rs);
        }
        return result;
    }

    protected Object getPropValue(ResultSet rs, int index, PropDesc prop) throws SQLException {
        if (transformMap != null) {
            Function<String, ?> decorator = transformMap.get(prop.getRawFieldName());
            if (decorator != null) {
                String value = rs.getString(index);
                return decorator.apply(value);
            }
        }
        Object value = JdbcUtils.getResultSetValue(rs, index, prop.getFieldClass());
        return DbaUtil.checkEnumPropValue(prop, value);
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

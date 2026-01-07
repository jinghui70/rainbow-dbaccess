package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class StringObjectFieldMapper<T> extends FieldMapper<T> {

    private final Class<T> fieldClass;

    private Class<?> componentClass;

    private ParameterizedType parameterizedType;

    public StringObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (JSON.class.isAssignableFrom(fieldClass)) return;
        if (fieldClass.isArray()) {
            componentClass = fieldClass.getComponentType();
        } else if (Collection.class.isAssignableFrom(fieldClass) || Map.class.isAssignableFrom(fieldClass)) {
            parameterizedType = (ParameterizedType) field.getGenericType();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public T formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        if (rs.wasNull()) return null;
        if (JSON.class.isAssignableFrom(fieldClass))
            return (T) JSONUtil.parse(str);
        if (componentClass != null)
            return (T) JSONUtil.parseArray(str).toArray(componentClass);
        if (parameterizedType != null)
            return JSONUtil.toBean(str, parameterizedType, false);
        return JSONUtil.toBean(str, fieldClass);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        String str = JSONUtil.toJsonStr(value);
        ps.setString(paramIndex, str);
    }

}

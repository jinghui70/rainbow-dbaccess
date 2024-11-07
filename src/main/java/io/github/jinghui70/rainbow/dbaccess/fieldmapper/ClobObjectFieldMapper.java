package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClobObjectFieldMapper<T> extends FieldMapper<T> {

    private final Class<T> fieldClass;

    private Class<?> componentClass;

    public ClobObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (fieldClass.isArray())
            componentClass = fieldClass.getComponentType();
        else if (field!=null && fieldClass.isAssignableFrom(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            componentClass = (Class<?>) actualTypeArguments[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T formDB(ResultSet rs, int index) throws SQLException {
        String json = rs.getString(index);
        if (rs.wasNull()) return null;
        if (fieldClass.isArray()) {
            JSONArray array = JSONUtil.parseArray(json);
            return (T) array.toArray(componentClass);
        }
        if (fieldClass.isAssignableFrom(List.class)) {
            JSONArray array = JSONUtil.parseArray(json);
            return (T) array.toList(componentClass);
        }
        return JSONUtil.toBean(json, fieldClass);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, Object value) throws SQLException {
       String json = JSONUtil.toJsonStr(value);
        ps.setString(paramIndex, json);
    }

    public static <T> ClobObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new ClobObjectFieldMapper<>(fieldClass, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ClobObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        ClobObjectFieldMapper<?> result = new ClobObjectFieldMapper<>(List.class, null);
        result.componentClass = componentClass;
        return  (ClobObjectFieldMapper<List<T>>) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> ClobObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        ClobObjectFieldMapper<?> result = new ClobObjectFieldMapper<>(Object[].class, null);
        result.componentClass = componentClass;
        return  (ClobObjectFieldMapper<T[]>) result;
    }
}

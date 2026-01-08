package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.lang.ParameterizedTypeImpl;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.github.jinghui70.rainbow.dbaccess.map.MapHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectFieldMapper<T> extends FieldMapper<T> {

    private Class<T> fieldClass;

    private Class<?> componentClass;

    private ParameterizedType parameterizedType;

    private ObjectFieldMapper() {
    }

    public ObjectFieldMapper(Class<T> fieldClass) {
        this.fieldClass = fieldClass;
    }

    public ObjectFieldMapper(TypeReference<T> type) {
        Assert.isInstanceOf(ParameterizedType.class, type.getType());
        this.parameterizedType = (ParameterizedType) type.getType();
    }

    public ObjectFieldMapper(Class<T> fieldClass, Field field) {
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
        if (componentClass != null)
            return (T) JSONUtil.parseArray(str).toArray(componentClass);
        if (parameterizedType != null)
            return JSONUtil.toBean(str, parameterizedType, false);
        Assert.notNull(fieldClass, "object field mapper class is null");
        if (JSON.class.isAssignableFrom(fieldClass))
            return (T) JSONUtil.parse(str);
        return JSONUtil.toBean(str, fieldClass);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        String str = JSONUtil.toJsonStr(value);
        ps.setString(paramIndex, str);
    }

    public static <T> ObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new ObjectFieldMapper<>(fieldClass);
    }

    public static <T> ObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        ObjectFieldMapper<T[]> result = new ObjectFieldMapper<>();
        result.componentClass = componentClass;
        return result;
    }

    public static <T> ObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        ObjectFieldMapper<List<T>> result = new ObjectFieldMapper<>();
        result.parameterizedType = new ParameterizedTypeImpl(new Type[]{componentClass}, null, List.class);
        return result;
    }

    public static ObjectFieldMapper<Map<String, Object>> ofMap() {
        return new ObjectFieldMapper<>(MapHandler.MAP_TYPE);
    }

}

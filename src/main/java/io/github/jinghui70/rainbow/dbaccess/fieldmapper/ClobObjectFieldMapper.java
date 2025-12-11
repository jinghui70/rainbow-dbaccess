package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.*;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ClobObjectFieldMapper<T> extends ClobFieldMapper<T> {

    private final Class<T> fieldClass;

    private Class<?> componentClass;

    private JSONConfig jsonConfig;

    public ClobObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (fieldClass.isArray())
            componentClass = fieldClass.getComponentType();
        else if (field != null && fieldClass.isAssignableFrom(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            componentClass = (Class<?>) actualTypeArguments[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T parse(String str) {
        if (JSON.class.isAssignableFrom(fieldClass)) {
            return (T) JSONUtil.parse(str, jsonConfig);
        }
        if (fieldClass.isArray()) {
            JSONArray array = JSONUtil.parseArray(str);
            return (T) array.toArray(componentClass);
        }
        if (fieldClass.isAssignableFrom(List.class)) {
            JSONArray array = JSONUtil.parseArray(str);
            return (T) array.toList(componentClass);
        }
        return JSONUtil.toBean(str, fieldClass);
    }

    @Override
    public String getString(@NonNull Object value) {
        return JSONUtil.toJsonStr(value);
    }

    public static <T> ClobObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new ClobObjectFieldMapper<>(fieldClass, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ClobObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        ClobObjectFieldMapper<?> result = new ClobObjectFieldMapper<>(List.class, null);
        result.componentClass = componentClass;
        return (ClobObjectFieldMapper<List<T>>) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> ClobObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        ClobObjectFieldMapper<?> result = new ClobObjectFieldMapper<>(Object[].class, null);
        result.componentClass = componentClass;
        return (ClobObjectFieldMapper<T[]>) result;
    }

    public static ClobObjectFieldMapper<JSONObject> ofJsonObject() {
        return new ClobObjectFieldMapper<>(JSONObject.class, null);
    }

    public static ClobObjectFieldMapper<JSONObject> ofJsonObject(JSONConfig config) {
        ClobObjectFieldMapper<JSONObject> result = new ClobObjectFieldMapper<>(JSONObject.class, null);
        result.jsonConfig = config;
        return result;
    }


    public static ClobObjectFieldMapper<JSONArray> ofJsonArray() {
        return new ClobObjectFieldMapper<>(JSONArray.class, null);
    }

    public static ClobObjectFieldMapper<JSONArray> ofJsonArray(JSONConfig config) {
        ClobObjectFieldMapper<JSONArray> result = new ClobObjectFieldMapper<>(JSONArray.class, null);
        result.jsonConfig = config;
        return result;
    }
}

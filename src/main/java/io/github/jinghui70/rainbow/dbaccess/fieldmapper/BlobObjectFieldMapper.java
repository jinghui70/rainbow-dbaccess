package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BlobObjectFieldMapper<T> extends BlobFieldMapper<T> {

    private final Class<T> fieldClass;

    private Class<?> componentClass;

    private JSONConfig jsonConfig;

    public BlobObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (JSON.class.isAssignableFrom(fieldClass)) return;
        if (fieldClass.isArray())
            componentClass = fieldClass.getComponentType();
        else if (field != null && fieldClass.isAssignableFrom(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            componentClass = (Class<?>) actualTypeArguments[0];
        }
    }

    @Override
    protected byte[] getBytes(Object value) {
        String json = JSONUtil.toJsonStr(value);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T parse(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        if (JSON.class.isAssignableFrom(fieldClass)) {
            return (T) JSONUtil.parse(json, jsonConfig);
        }
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

    public static <T> BlobObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new BlobObjectFieldMapper<>(fieldClass, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> BlobObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        BlobObjectFieldMapper<?> result = new BlobObjectFieldMapper<>(List.class, null);
        result.componentClass = componentClass;
        return (BlobObjectFieldMapper<List<T>>) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> BlobObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        BlobObjectFieldMapper<?> result = new BlobObjectFieldMapper<>(Object[].class, null);
        result.componentClass = componentClass;
        return (BlobObjectFieldMapper<T[]>) result;
    }

    public static BlobObjectFieldMapper<JSONObject> ofJsonObject() {
        return new BlobObjectFieldMapper<>(JSONObject.class, null);
    }

    public static BlobObjectFieldMapper<JSONObject> ofJsonObject(JSONConfig config) {
        BlobObjectFieldMapper<JSONObject> result = new BlobObjectFieldMapper<>(JSONObject.class, null);
        result.jsonConfig = config;
        return result;
    }

    public static BlobObjectFieldMapper<JSONArray> ofJsonArray() {
        return new BlobObjectFieldMapper<>(JSONArray.class, null);
    }

    public static BlobObjectFieldMapper<JSONArray> ofJsonArray(JSONConfig config) {
        BlobObjectFieldMapper<JSONArray> result = new BlobObjectFieldMapper<>(JSONArray.class, null);
        result.jsonConfig = config;
        return result;
    }
}

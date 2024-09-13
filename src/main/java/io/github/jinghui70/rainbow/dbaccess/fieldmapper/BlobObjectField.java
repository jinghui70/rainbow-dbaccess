package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BlobObjectField<T> extends BlobField<T> {

    private final Class<T> fieldClass;

    private Class<?> comonentClass;

    public BlobObjectField(Class<T> fieldClass) {
        this.fieldClass = fieldClass;
        if (fieldClass.isArray())
            comonentClass = fieldClass.getComponentType();
    }

    public BlobObjectField(Class<T> fieldClass, Class<?> componentClass) {
        this.fieldClass = fieldClass;
        this.comonentClass = componentClass;
    }

    public BlobObjectField(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (fieldClass.isAssignableFrom(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            comonentClass = (Class<?>) actualTypeArguments[0];
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
        if (fieldClass.isArray()) {
            JSONArray array = JSONUtil.parseArray(json);
            return (T) array.toArray(comonentClass);
        }
        if (fieldClass.isAssignableFrom(List.class)) {
            JSONArray array = JSONUtil.parseArray(json);
            return (T) array.toList(comonentClass);
        }
        return JSONUtil.toBean(json, fieldClass);
    }

}

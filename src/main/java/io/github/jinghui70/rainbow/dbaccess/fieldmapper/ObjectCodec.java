package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.lang.ParameterizedTypeImpl;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * JSON 对象编解码器 — 负责 Java 对象与 JSON 字符串之间的双向转换。
 * <p>
 * 同时提供 {@code of / ofArray / ofList / ofMap} 工厂方法，
 * 供 {@link ObjectFieldMapper} 和 {@link BlobObjectFieldMapper} 共用，
 * 消除二者之间重复的序列化逻辑。
 *
 * @param <T> 目标类型
 */
public class ObjectCodec<T> {

    static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    // 对象类型
    private final Class<T> fieldClass;
    // 数组元素类型
    private final Class<?> componentClass;
    // 复杂对象类型，如 List，Map
    private final Type complexType;

    ObjectCodec(Class<T> fieldClass, Class<?> componentClass, Type complexType) {
        this.fieldClass = fieldClass;
        this.componentClass = componentClass;
        this.complexType = complexType;
    }

    /**
     * 根据实体字段的反射信息构造 — 用于 {@code PropInfoCache} 自动匹配。
     */
    ObjectCodec(Class<T> fieldClass, Field field) {
        this.fieldClass = fieldClass;
        if (JSON.class.isAssignableFrom(fieldClass)) {
            this.componentClass = null;
            this.complexType = null;
            return;
        }
        if (fieldClass.isArray()) {
            this.componentClass = fieldClass.getComponentType();
            this.complexType = null;
        } else if (Collection.class.isAssignableFrom(fieldClass) || Map.class.isAssignableFrom(fieldClass)) {
            this.componentClass = null;
            this.complexType = field.getGenericType();
        } else {
            this.componentClass = null;
            this.complexType = null;
        }
    }

    // ────────────────────────── 序列化 / 反序列化 ──────────────────────────

    public String toJson(Object value) {
        if (value instanceof Enum<?> e) {
            return "\"" + e.name() + "\"";
        }
        return JSONUtil.toJsonStr(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public T parse(String str) {
        if (componentClass != null)
            return (T) JSONUtil.parseArray(str).toArray(componentClass);
        if (complexType != null)
            return JSONUtil.toBean(str, complexType, false);
        if (JSON.class.isAssignableFrom(fieldClass))
            return (T) JSONUtil.parse(str);
        if (fieldClass.isEnum()) {
            String name = str;
            if (name.startsWith("\"") && name.endsWith("\""))
                name = name.substring(1, name.length() - 1);
            return (T) Enum.valueOf((Class<Enum>) fieldClass, name);
        }
        return JSONUtil.toBean(str, fieldClass);
    }

    // ────────────────────────── 工厂方法 ──────────────────────────

    public static <T> ObjectCodec<T> of(Class<T> fieldClass) {
        return new ObjectCodec<>(fieldClass, null, null);
    }

    public static <T> ObjectCodec<T[]> ofArray(Class<T> componentClass) {
        return new ObjectCodec<>(null, componentClass, null);
    }

    public static <T> ObjectCodec<List<T>> ofList(Class<T> componentClass) {
        return new ObjectCodec<>(null, null,
                new ParameterizedTypeImpl(new Type[]{componentClass}, null, List.class));
    }

    public static <T> ObjectCodec<Map<String, T>> ofMap(Class<T> valueClass) {
        return new ObjectCodec<>(null, null,
                new ParameterizedTypeImpl(new Type[]{String.class, valueClass }, null, Map.class));
    }

    public static <T> ObjectCodec<T> ofType(Type complexType) {
        return new ObjectCodec<>(null, null, complexType);
    }
}

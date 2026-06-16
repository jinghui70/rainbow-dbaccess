package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * BLOB JSON 对象字段映射器。
 * <p>
 * 将 Java 对象序列化为 JSON 字符串后以 UTF-8 编码存储为 BLOB，
 * 读取时从 BLOB 解码为 JSON 字符串再反序列化为 Java 对象。
 *
 * @param <T> 目标 Java 类型
 */
public class BlobObjectFieldMapper<T> extends BlobFieldMapper<T> {

    private final ObjectCodec<T> codec;

    /**
     * 使用指定的编解码器构造。
     *
     * @param codec 对象编解码器
     */
    private BlobObjectFieldMapper(ObjectCodec<T> codec) {
        this.codec = codec;
    }

    /**
     * 根据字段类型和反射信息构造。
     *
     * @param fieldClass 字段类型
     * @param field      反射字段对象
     */
    public BlobObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.codec = new ObjectCodec<>(fieldClass, field);
    }

    /**
     * 将 Java 值转换为 UTF-8 编码的 JSON 字节数组。
     *
     * @param value Java 值
     * @return JSON 字符串的 UTF-8 字节数组
     */
    @Override
    protected byte[] getBytes(Object value) {
        String json = codec.toJson(value);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从 UTF-8 字节数组解析 JSON 字符串并反序列化为 Java 对象。
     *
     * @param bytes UTF-8 编码的 JSON 字节数组
     * @return 反序列化后的 Java 对象
     */
    @Override
    protected T parse(byte[] bytes) {
        String str = new String(bytes, StandardCharsets.UTF_8);
        return codec.parse(str);
    }

    /**
     * 创建指定类型的 BLOB 对象字段映射器。
     *
     * @param fieldClass 目标类型
     * @param <T>        目标类型
     * @return BlobObjectFieldMapper 实例
     */
    public static <T> BlobObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.of(fieldClass));
    }

    /**
     * 创建数组类型的 BLOB 对象字段映射器。
     *
     * @param componentClass 数组元素类型
     * @param <T>            数组元素类型
     * @return BlobObjectFieldMapper 实例
     */
    public static <T> BlobObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofArray(componentClass));
    }

    /**
     * 创建 List 类型的 BLOB 对象字段映射器。
     *
     * @param componentClass List 元素类型
     * @param <T>            List 元素类型
     * @return BlobObjectFieldMapper 实例
     */
    public static <T> BlobObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofList(componentClass));
    }

    /**
     * 创建 Map 类型的 BLOB 对象字段映射器。
     *
     * @param valueClass Map 值类型
     * @param <T>        Map 值类型
     * @return BlobObjectFieldMapper 实例
     */
    public static <T> BlobObjectFieldMapper<Map<String, T>> ofMap(Class<T> valueClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofMap(valueClass));
    }

    /**
     * 创建复杂泛型类型的 BLOB 对象字段映射器。
     *
     * @param complexType 复杂类型
     * @param <T>         目标类型
     * @return BlobObjectFieldMapper 实例
     */
    public static <T> BlobObjectFieldMapper<T> ofMap(Type complexType) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofType(complexType));
    }
}

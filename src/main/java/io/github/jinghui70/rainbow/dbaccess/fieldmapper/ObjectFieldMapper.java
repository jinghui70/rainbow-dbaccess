package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JSON 对象字段映射器。
 * <p>
 * 将 Java 对象序列化为 JSON 字符串存储为数据库 VARCHAR/CLOB 类型，
 * 读取时从 JSON 字符串反序列化为 Java 对象。
 *
 * @param <T> 目标 Java 类型
 */
public class ObjectFieldMapper<T> extends FieldMapper<T> {

    private final ObjectCodec<T> codec;

    /**
     * 使用指定的编解码器构造。
     *
     * @param codec 对象编解码器
     */
    private ObjectFieldMapper(ObjectCodec<T> codec) {
        this.codec = codec;
    }

    /**
     * 根据字段类型和反射信息构造。
     *
     * @param fieldClass 字段类型
     * @param field      反射字段对象
     */
    public ObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.codec = new ObjectCodec<>(fieldClass, field);
    }

    /**
     * 从 ResultSet 读取 JSON 字符串并反序列化为 Java 对象。
     *
     * @param rs    JDBC ResultSet
     * @param index 列索引
     * @return 反序列化后的 Java 对象
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        return codec.parse(str);
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串并保存到 PreparedStatement 中。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @param value      要保存的 Java 对象
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        String str = codec.toJson(value);
        ps.setString(paramIndex, str);
    }

    /**
     * 创建指定类型的对象字段映射器。
     *
     * @param fieldClass 目标类型
     * @param <T>        目标类型
     * @return ObjectFieldMapper 实例
     */
    public static <T> ObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new ObjectFieldMapper<>(ObjectCodec.of(fieldClass));
    }

    /**
     * 创建数组类型的对象字段映射器。
     *
     * @param componentClass 数组元素类型
     * @param <T>            数组元素类型
     * @return ObjectFieldMapper 实例
     */
    public static <T> ObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofArray(componentClass));
    }

    /**
     * 创建 List 类型的对象字段映射器。
     *
     * @param componentClass List 元素类型
     * @param <T>            List 元素类型
     * @return ObjectFieldMapper 实例
     */
    public static <T> ObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofList(componentClass));
    }

    /**
     * 创建 Map 类型的对象字段映射器。
     *
     * @param valueClass Map 值类型
     * @param <T>        Map 值类型
     * @return ObjectFieldMapper 实例
     */
    public static <T> ObjectFieldMapper<Map<String, T>> ofMap(Class<T> valueClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofMap(valueClass));
    }

    /**
     * 创建复杂泛型类型的对象字段映射器。
     *
     * @param complexType 复杂类型
     * @param <T>         目标类型
     * @return ObjectFieldMapper 实例
     */
    public static <T> ObjectFieldMapper<T> ofMap(Type complexType) {
        return new ObjectFieldMapper<>(ObjectCodec.ofType(complexType));
    }
}

package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import io.github.jinghui70.rainbow.dbaccess.CodeEnum;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 枚举类型字段映射器。
 * <p>
 * 支持两种枚举存储方式：
 * <ul>
 *     <li>普通枚举：使用枚举的 {@code name()} 存储为字符串</li>
 *     <li>实现 {@link CodeEnum} 的枚举：使用 {@code code()} 的返回值存储</li>
 * </ul>
 *
 * @param <T> 枚举类型
 */
public class EnumFieldMapper<T extends Enum<T>> extends FieldMapper<T> {

    private final Class<T> enumClass;

    private final boolean isCode;

    /**
     * 构造枚举字段映射器。
     *
     * @param enumClass 枚举类型
     */
    public EnumFieldMapper(Class<T> enumClass) {
        this.enumClass = enumClass;
        isCode = CodeEnum.class.isAssignableFrom(enumClass);
    }

    /**
     * 从 ResultSet 读取字符串并转换为枚举值。
     * <p>
     * 如果枚举实现了 {@link CodeEnum}，则根据 code 匹配；
     * 否则根据枚举名称匹配。
     *
     * @param rs    JDBC ResultSet
     * @param index 列索引
     * @return 枚举值，如果为 SQL NULL 或找不到匹配则返回 {@code null}
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        String value = rs.getString(index);
        if (value == null) return null;
        if (isCode) {
            for (T t : enumClass.getEnumConstants()) {
                if (Objects.equals(value, ((CodeEnum) t).code()))
                    return t;
            }
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    /**
     * 将枚举值保存到 PreparedStatement 中。
     * <p>
     * 如果枚举实现了 {@link CodeEnum}，则存储 code；
     * 否则存储枚举名称。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @param value      要保存的枚举值
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        if (isCode)
            ps.setString(paramIndex, ((CodeEnum) value).code());
        else
            ps.setString(paramIndex, ((Enum<?>) value).name());
    }

    /**
     * 创建枚举字段映射器的工厂方法。
     *
     * @param enumClass 枚举类型
     * @param <T>       枚举类型
     * @return EnumFieldMapper 实例
     */
    public static <T extends Enum<T>> EnumFieldMapper<T> of(Class<T> enumClass) {
        return new EnumFieldMapper<>(enumClass);
    }
}

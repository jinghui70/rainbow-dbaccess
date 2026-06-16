package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 布尔类型字段映射器。
 * <p>
 * 将 Java 的 {@code Boolean} 与数据库的数字类型（1/0）进行互相转换：
 * <ul>
 *     <li>{@code true} 存储为 1</li>
 *     <li>{@code false} 存储为 0</li>
 * </ul>
 */
public class BoolFieldMapper extends FieldMapper<Boolean> {

    /**
     * 从 ResultSet 读取布尔值。
     *
     * @param rs    JDBC ResultSet
     * @param index 列索引
     * @return 布尔值，如果为 SQL NULL 则返回 {@code null}
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public Boolean formDB(ResultSet rs, int index) throws SQLException {
        boolean value = rs.getBoolean(index);
        if (rs.wasNull()) return null;
        return value;
    }

    /**
     * 将布尔值保存到 PreparedStatement 中（使用 1/0 表示）。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @param value      要保存的布尔值
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        value = Boolean.TRUE.equals(value) ? 1 : 0;
        super.saveToDB(ps, paramIndex, value);
    }

    /**
     * 单例实例。
     */
    public static final BoolFieldMapper INSTANCE = new BoolFieldMapper();
}

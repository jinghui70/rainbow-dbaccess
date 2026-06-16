package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Y/N 布尔类型字段映射器。
 * <p>
 * 将 Java 的 {@code Boolean} 与数据库的字符串类型（"Y"/"N"）进行互相转换：
 * <ul>
 *     <li>{@code true} 存储为 "Y"</li>
 *     <li>{@code false} 存储为 "N"</li>
 * </ul>
 */
public class BoolYN extends FieldMapper<Boolean> {

    /**
     * 从 ResultSet 读取 Y/N 字符串并转换为布尔值。
     *
     * @param rs    JDBC ResultSet
     * @param index 列索引
     * @return 布尔值，"Y" 返回 {@code true}，其他返回 {@code false}，SQL NULL 返回 {@code null}
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public Boolean formDB(ResultSet rs, int index) throws SQLException {
        String value = rs.getString(index);
        if (rs.wasNull()) return null;
        return "Y".equals(value);
    }

    /**
     * 将布尔值保存到 PreparedStatement 中（使用 "Y"/"N" 表示）。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @param value      要保存的布尔值
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        value = Boolean.TRUE.equals(value) ? "Y" : "N";
        super.saveToDB(ps, paramIndex, value);
    }

}

package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于使用指定的 FieldMapper 设置 sql 参数的对象
 */
public class FieldValue {

    private final Object value;

    private final FieldMapper<?> mapper;

    /**
     * 构造 FieldValue 对象。
     *
     * @param value  字段值
     * @param mapper 字段映射器
     */
    public FieldValue(Object value, FieldMapper<?> mapper) {
        this.value = value;
        this.mapper = mapper;
    }

    /**
     * 使用关联的 FieldMapper 将值设置到 PreparedStatement 中。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @throws SQLException 如果数据库访问出错
     */
    public void setParameter(PreparedStatement ps, int paramIndex) throws SQLException {
        mapper.saveToDB(ps, paramIndex, value);
    }

    /**
     * 返回字段值。
     *
     * @return 字段值
     */
    public Object getValue() {
        return value;
    }
}

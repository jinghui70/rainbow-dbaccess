package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于使用指定的 FieldMapper 设置 sql 参数的对象
 */
public class FieldValue {

    private final Object value;

    private final FieldMapper<?> mapper;

    public FieldValue(Object value, FieldMapper<?> mapper) {
        this.value = value;
        this.mapper = mapper;
    }

    public void setParameter(PreparedStatement ps, int paramIndex) throws SQLException {
        mapper.saveToDB(ps, paramIndex, value);
    }

    public Object getValue() {
        return value;
    }
}

package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FieldValue {

    private final Object value;

    private final FieldMapper<?> mapper;

    public FieldValue(Object value, FieldMapper<?> mapper) {
        this.value = value;
        this.mapper = mapper;
    }

    public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
        mapper.saveToDB(ps, paramIndex, value);
    }

}

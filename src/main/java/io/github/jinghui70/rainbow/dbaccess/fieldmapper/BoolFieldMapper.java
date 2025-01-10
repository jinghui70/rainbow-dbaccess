package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoolFieldMapper extends FieldMapper<Boolean> {

    @Override
    public Boolean formDB(ResultSet rs, int index) throws SQLException {
        boolean value = rs.getBoolean(index);
        if (rs.wasNull()) return null;
        return value;
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        value = Boolean.TRUE.equals(value) ? 1 : 0;
        super.saveToDB(ps, paramIndex, value);
    }

    public static BoolFieldMapper INSTANCE = new BoolFieldMapper();
}

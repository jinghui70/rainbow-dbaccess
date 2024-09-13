package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class FieldMapper<T> {

    public abstract T formDB(ResultSet rs, int index) throws SQLException;

    public void saveToDB(PreparedStatement ps, int paramIndex, Object value) throws SQLException {
        ps.setObject(paramIndex, value);
    }

}

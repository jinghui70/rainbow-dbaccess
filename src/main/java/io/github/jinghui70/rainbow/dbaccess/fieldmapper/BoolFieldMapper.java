package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BoolFieldMapper extends FieldMapper<Boolean> {

    protected abstract String getTrue();

    protected abstract String getFalse();

    @Override
    public Boolean formDB(ResultSet rs, int index) throws SQLException {
        String value = rs.getString(index);
        if (rs.wasNull()) return null;
        return getTrue().equals(value.trim());
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, Object value) throws SQLException {
        value = Boolean.TRUE.equals(value) ? getTrue() : getFalse();
        super.saveToDB(ps, paramIndex, value);
    }
}

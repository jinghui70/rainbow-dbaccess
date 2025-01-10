package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoolYN extends FieldMapper<Boolean> {

    @Override
    public Boolean formDB(ResultSet rs, int index) throws SQLException {
        String value = rs.getString(index);
        if (rs.wasNull()) return null;
        return "Y".equals(value);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        value = Boolean.TRUE.equals(value) ? "Y" : "N";
        super.saveToDB(ps, paramIndex, value);
    }

}

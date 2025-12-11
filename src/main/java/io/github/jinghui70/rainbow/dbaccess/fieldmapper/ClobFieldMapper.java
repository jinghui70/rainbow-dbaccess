package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.*;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class ClobFieldMapper<T> extends FieldMapper<T> {

    protected abstract String getString(Object value);

    protected abstract T parse(String str);

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        if (rs.wasNull()) return null;
        return parse(str);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        String str = getString(value);
        ps.setString(paramIndex, str);
    }

}

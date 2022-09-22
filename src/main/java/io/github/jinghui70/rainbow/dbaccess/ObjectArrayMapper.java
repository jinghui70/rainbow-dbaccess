package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ObjectArrayMapper implements RowMapper<Object[]> {

    public static ObjectArrayMapper INSTANCE = new ObjectArrayMapper();

    @Override
    public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Object[] result = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            result[i - 1] = JdbcUtils.getResultSetValue(rs, i);
        }
        return result;
    }

}

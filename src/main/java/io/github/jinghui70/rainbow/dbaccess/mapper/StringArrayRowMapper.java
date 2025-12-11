package io.github.jinghui70.rainbow.dbaccess.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class StringArrayRowMapper implements RowMapper<String[]> {

    @Override
    public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] result = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            int columnIndex = i + 1;
            result[i] = rs.getString(columnIndex);
        }
        return result;
    }

}

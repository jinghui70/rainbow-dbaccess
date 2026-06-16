package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 将数据库行映射为String数组的RowMapper。
 * 每列都通过ResultSet.getString()获取。
 */
public class StringArrayRowMapper implements RowMapper<String[]> {

    /**
     * 将ResultSet的当前行映射为String数组。
     *
     * @param rs ResultSet对象
     * @param rowNum 行号
     * @return 包含列值的String数组
     * @throws SQLException 如果发生SQL异常
     */
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

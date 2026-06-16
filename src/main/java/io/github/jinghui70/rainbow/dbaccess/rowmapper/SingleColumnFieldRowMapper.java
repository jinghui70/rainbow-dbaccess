package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 单列结果的RowMapper，使用指定的FieldMapper进行转换。
 * 要求结果集必须只有一列。
 *
 * @param <T> 转换后的目标类型
 */
public class SingleColumnFieldRowMapper<T> implements RowMapper<T> {

    private final FieldMapper<T> fieldMapper;

    /**
     * 构造函数。
     *
     * @param fieldMapper 字段映射器
     */
    public SingleColumnFieldRowMapper(FieldMapper<T> fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

    /**
     * 将ResultSet的当前行映射为指定类型的对象。
     * 要求结果集必须只有一列。
     *
     * @param rs ResultSet对象
     * @param rowNum 行号
     * @return 转换后的对象
     * @throws SQLException 如果发生SQL异常，或列数不等于1
     */
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Validate column count.
        ResultSetMetaData metaData = rs.getMetaData();
        int nrOfColumns = metaData.getColumnCount();
        if (nrOfColumns != 1) {
            throw new IncorrectResultSetColumnCountException(1, nrOfColumns);
        }
        // Extract column value from JDBC ResultSet.
        return fieldMapper.formDB(rs, 1);
    }
}

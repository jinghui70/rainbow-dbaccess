package io.github.jinghui70.rainbow.dbaccess.mapper;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SingleColumnFieldRowMapper<T> implements RowMapper<T> {

    private final FieldMapper<T> fieldMapper;

    public SingleColumnFieldRowMapper(FieldMapper<T> fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

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

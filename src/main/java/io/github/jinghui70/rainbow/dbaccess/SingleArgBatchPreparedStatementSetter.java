package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SingleArgBatchPreparedStatementSetter<T> implements BatchPreparedStatementSetter {

    private List<T> list;

    private int colType = SqlTypeValue.TYPE_UNKNOWN;

    public SingleArgBatchPreparedStatementSetter(List<T> list) {
        this.list = list;
    }

    public SingleArgBatchPreparedStatementSetter(List<T> list, int colType) {
        this.list = list;
        this.colType = colType;
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        StatementCreatorUtils.setParameterValue(ps, 1, colType, list.get(i));
    }

    @Override
    public int getBatchSize() {
        return list.size();
    }
}

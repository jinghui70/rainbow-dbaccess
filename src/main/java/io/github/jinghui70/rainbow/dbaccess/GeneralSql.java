package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
@SuppressWarnings("unchecked")
public abstract class GeneralSql<S extends GeneralSql<S>> extends SqlWrapper<S> {

    private final List<Object> params = new ArrayList<>();

    protected GeneralSql() {
        super();
    }

    protected GeneralSql(String str) {
        super(str);
    }

    protected GeneralSql(Dba dba) {
        super(dba);
    }

    protected JdbcTemplate getJdbcTemplate() {
        return dba.getJdbcTemplate();
    }

    public S from(GeneralSql<?> sql) {
        append(" FROM (");
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        append(")");
        return (S) this;
    }

    public S append(GeneralSql<?> sql) {
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return (S) this;
    }

    @Override
    public S set(String field, Object value) {
        set();
        append(field).append("=?").addParam(enumCheck(value));
        return (S) this;
    }

    @Override
    public S append(Cnd cnd) {
        cnd.toSql(this);
        return (S) this;
    }

    public List<Object> getParams() {
        return params;
    }


    public S addParam(Object... params) {
        Collections.addAll(this.params, params);
        return (S) this;
    }

    public S addParams(Collection<Object> params) {
        this.params.addAll(params);
        return (S) this;
    }

    public S setParam(Object... params) {
        this.params.clear();
        return addParam(params);
    }

    public S setParams(Collection<Object> params) {
        this.params.clear();
        return addParams(params);
    }

    public boolean noParams() {
        return params.isEmpty();
    }

    private Object[] getParamArray() {
        return params.toArray();
    }

    /**
     * 执行当前sql
     *
     * @return 执行影响的行数
     */
    @Override
    public int execute() {
        if (noParams())
            return getJdbcTemplate().update(getSql());
        else
            return getJdbcTemplate().update(getSql(), getParamArray());
    }

    public int[] batchUpdate(List<Object[]> batchArgs) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return getJdbcTemplate().batchUpdate(
                getSql(),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] values = batchArgs.get(i);
                        int colIndex = 1;
                        for (Object value : values) {
                            DbaUtil.setParameterValue(ps, colIndex++, value, nullTypeCache);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );
    }

    public int[][] batchUpdate(List<Object[]> batchArgs, int batchSize) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return getJdbcTemplate().batchUpdate(getSql(), batchArgs, batchSize,
                (ps, argument) -> {
                    int colIndex = 1;
                    for (Object value : argument) {
                        DbaUtil.setParameterValue(ps, colIndex++, value, nullTypeCache);
                    }
                });
    }

    @Override
    public void query(RowCallbackHandler rch) {
        if (noParams())
            getJdbcTemplate().query(getSql(), rch);
        else
            getJdbcTemplate().query(getSql(), rch, getParamArray());
    }

    @Override
    protected <T> T queryForValue(String sql, Class<T> requiredType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForObject(sql, requiredType);
        else
            return getJdbcTemplate().queryForObject(sql, requiredType, getParamArray());
    }

    @Override
    public <T> T queryForObject(RowMapper<T> mapper) throws DataAccessException {
        try {
            if (noParams())
                return getJdbcTemplate().queryForObject(getSql(), mapper);
            else
                return getJdbcTemplate().queryForObject(getSql(), mapper, getParamArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().query(sql, rowMapper);
        else
            return getJdbcTemplate().query(sql, rowMapper, getParamArray());
    }

    @Override
    protected <T> List<T> queryForValueList(String sql, Class<T> elementType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForList(sql, elementType);
        else
            return getJdbcTemplate().queryForList(sql, elementType, getParamArray());
    }

}
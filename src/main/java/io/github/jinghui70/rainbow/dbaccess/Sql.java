package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
public class Sql extends SqlWrapper<Sql> {

    private final List<Object> params = new ArrayList<>();

    protected Sql() {
        super();
    }

    public Sql(String str) {
        super(str);
    }

    public Sql(Dba dba) {
        super(dba);
    }

    public static Sql create() {
        return new Sql();
    }

    public static Sql create(String str) {
        return new Sql(str);
    }

    protected JdbcTemplate getJdbcTemplate() {
        return dba.getJdbcTemplate();
    }

    public Sql from(Sql sql) {
        append(" FROM (");
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return append(")");
    }

    public Sql append(Sql sql) {
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return this;
    }

    @Override
    public Sql set(String field, Object value) {
        set();
        return append(field).append("=?").addParam(value);
    }

    @Override
    public Sql append(Cnd cnd) {
        cnd.toSql(this);
        return this;
    }

    public List<Object> getParams() {
        return params;
    }

    public Sql addParam(Object... params) {
        Collections.addAll(this.params, params);
        return this;
    }

    public Sql addParams(Collection<Object> params) {
        this.params.addAll(params);
        return this;
    }

    public Object[] getParamArray() {
        return params.toArray();
    }

    public Sql setParam(Object... params) {
        this.params.clear();
        return addParam(params);
    }

    public boolean noParams() {
        return params.isEmpty();
    }

    /**
     * 执行当前sql
     *
     * @return 执行影响的行数
     */
    public int execute() {
        if (noParams())
            return getJdbcTemplate().update(getSql());
        else
            return getJdbcTemplate().update(getSql(), getParamArray());
    }

    public int[] batchUpdate(List<Object[]> batchArgs) {
        return getJdbcTemplate().batchUpdate(getSql(), batchArgs);
    }

    public int[] batchUpdate(List<Object[]> batchArgs, int[] argTypes) {
        return getJdbcTemplate().batchUpdate(getSql(), batchArgs, argTypes);
    }

    @Override
    public void query(RowCallbackHandler rch) {
        if (noParams())
            getJdbcTemplate().query(getSql(), rch);
        else
            getJdbcTemplate().query(getSql(), rch, getParamArray());
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
    protected <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().query(sql, rowMapper);
        else
            return getJdbcTemplate().query(sql, rowMapper, getParamArray());
    }

    @Override
    public <T> T queryForValue(String sql, Class<T> requiredType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForObject(sql, requiredType);
        else
            return getJdbcTemplate().queryForObject(sql, requiredType, getParamArray());
    }

    @Override
    public <T> List<T> queryForValueList(Class<T> elementType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForList(getSql(), elementType);
        else
            return getJdbcTemplate().queryForList(getSql(), elementType, getParamArray());
    }
}
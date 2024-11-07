package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 封装了一个Sql的内容对象基类
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

    public List<Object> getParams() {
        return params;
    }

    /**
     * 添加参数
     *
     * @param params 参数
     * @return this
     */
    public S addParam(Object... params) {
        Collections.addAll(this.params, params);
        return (S) this;
    }

    /**
     * 添加参数
     *
     * @param params 参数列表
     * @return this
     */
    public S addParams(List<Object> params) {
        this.params.addAll(params);
        return (S) this;
    }

    /**
     * 重置参数
     * @param params 参数
     * @return this
     */
    public S setParam(Object... params) {
        this.params.clear();
        return addParam(params);
    }

    /**
     * 重置参数
     * @param params 参数列表
     * @return this
     */
    public S setParams(List<Object> params) {
        this.params.clear();
        return addParams(params);
    }

    public boolean noParams() {
        return params.isEmpty();
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

    public S append(GeneralSql<?> sql) {
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return (S) this;
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
            return getJdbcTemplate().update(getSql(), new ArgumentSetter(params));
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
            getJdbcTemplate().query(getSql(), new ArgumentSetter(params), rch);
    }

    protected <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
        return noParams() ? getJdbcTemplate().query(sql, rse) :
                getJdbcTemplate().query(sql, new ArgumentSetter(params), rse);
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> mapper) throws DataAccessException {
        ResultSetExtractor<List<T>> extractor = new RowMapperResultSetExtractor<>(mapper, 1);
        try {
            List<T> results = query(sql, extractor);
            return DataAccessUtils.nullableSingleResult(results);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

}
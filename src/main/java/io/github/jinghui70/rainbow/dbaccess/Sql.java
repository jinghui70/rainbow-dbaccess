package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
public class Sql extends SqlWrapper<Sql> {

    private final List<Object> params = new ArrayList<>();

    private List<Integer> types = null;

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
        return append(field).append("=?").addParam(enumCheck(value));
    }

    @Override
    public Sql append(Cnd cnd) {
        cnd.toSql(this);
        return this;
    }

    public List<Object> getParams() {
        return params;
    }

    public Sql addTypeParam(Object param, int sqlType) {
        if (sqlType == SqlTypeValue.TYPE_UNKNOWN) return addParam(param);
        if (types == null)
            types = new ArrayList<>();
        int size = params.size() - types.size();
        if (size > 0)
            types.addAll(Collections.nCopies(size, SqlTypeValue.TYPE_UNKNOWN));
        params.add(param);
        types.add(sqlType);
        return this;
    }

    public Sql addParam(Object... params) {
        Collections.addAll(this.params, params);
        return this;
    }

    public Sql addParams(Collection<Object> params) {
        this.params.addAll(params);
        return this;
    }

    public Sql setParam(Object... params) {
        this.params.clear();
        return addParam(params);
    }

    public boolean noParams() {
        return params.isEmpty();
    }

    private Object[] getParamArray() {
        return params.toArray();
    }

    private int[] getTypeArray() {
        int size = params.size() - types.size();
        if (size > 0)
            types.addAll(Collections.nCopies(size, SqlTypeValue.TYPE_UNKNOWN));
        return types.stream().mapToInt(Integer::intValue).toArray();
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
        else if (types == null)
            return getJdbcTemplate().update(getSql(), getParamArray());
        else
            return getJdbcTemplate().update(getSql(), getParamArray(), getTypeArray());
    }

    public int[] batchUpdate(List<Object[]> batchArgs) {
        return batchUpdate(batchArgs, new int[0]);
    }

    public int[] batchUpdate(List<Object[]> batchArgs, int[] argTypes) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return getJdbcTemplate().batchUpdate(
                getSql(),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] values = batchArgs.get(i);
                        int colIndex = 0;
                        for (Object value : values) {
                            colIndex++;
                            int colType;
                            if (argTypes.length < colIndex) {
                                colType = SqlTypeValue.TYPE_UNKNOWN;
                            } else {
                                colType = argTypes[colIndex - 1];
                            }
                            DbaUtil.setParameterValue(ps, colIndex, colType, value, nullTypeCache);
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
        return batchUpdate(batchArgs, batchSize, new int[0]);
    }

    public int[][] batchUpdate(List<Object[]> batchArgs, int batchSize, final int[] argTypes) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return getJdbcTemplate().batchUpdate(getSql(), batchArgs, batchSize,
                (ps, argument) -> {
                    int colIndex = 0;
                    for (Object value : argument) {
                        colIndex++;
                        int colType;
                        if (argTypes.length < colIndex) {
                            colType = SqlTypeValue.TYPE_UNKNOWN;
                        } else {
                            colType = argTypes[colIndex - 1];
                        }
                        DbaUtil.setParameterValue(ps, colIndex, colType, value, nullTypeCache);
                    }
                });
    }

    @Override
    public void query(RowCallbackHandler rch) {
        if (noParams())
            getJdbcTemplate().query(getSql(), rch);
        else if (types == null)
            getJdbcTemplate().query(getSql(), rch, getParamArray());
        else
            getJdbcTemplate().query(getSql(), getParamArray(), getTypeArray(), rch);
    }

    @Override
    public <T> T queryForObject(RowMapper<T> mapper) throws DataAccessException {
        try {
            if (noParams())
                return getJdbcTemplate().queryForObject(getSql(), mapper);
            else if (types == null)
                return getJdbcTemplate().queryForObject(getSql(), mapper, getParamArray());
            else
                return getJdbcTemplate().queryForObject(getSql(), getParamArray(), getTypeArray(), mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    protected <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().query(sql, rowMapper);
        else if (types == null)
            return getJdbcTemplate().query(sql, rowMapper, getParamArray());
        else
            return getJdbcTemplate().query(sql, getParamArray(), getTypeArray(), rowMapper);
    }

    @Override
    protected <T> T queryForValue(String sql, Class<T> requiredType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForObject(sql, requiredType);
        else if (types == null)
            return getJdbcTemplate().queryForObject(sql, requiredType, getParamArray());
        else
            return getJdbcTemplate().queryForObject(sql, getParamArray(), getTypeArray(), requiredType);
    }

    @Override
    protected <T> List<T> queryForValueList(String sql, Class<T> elementType) throws DataAccessException {
        if (noParams())
            return getJdbcTemplate().queryForList(sql, elementType);
        else if (types == null)
            return getJdbcTemplate().queryForList(sql, elementType, getParamArray());
        else
            return getJdbcTemplate().queryForList(sql, getParamArray(), getTypeArray(), elementType);
    }
}
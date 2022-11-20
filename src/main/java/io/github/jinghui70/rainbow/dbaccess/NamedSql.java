package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.map.MapUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
public class NamedSql extends SqlWrapper<NamedSql> {

    private final Map<String, Object> params = new HashMap<>();

    public NamedSql() {
        super();
    }

    public NamedSql(String str) {
        super(str);
    }

    public NamedSql(Dba dao) {
        super(dao);
    }

    public NamedSql append(NamedSql sql) {
        append(sql.getSql());
        this.params.putAll(sql.getParams());
        return this;
    }

    @Override
    public NamedSql append(Cnd cnd) {
        cnd.toNamedSql(this);
        return this;
    }

    @Override
    public NamedSql set(String key) {
        if (key.contains(":=")) return super.set(key);
        set();
        append(key).append("=:").append(key);
        return this;
    }

    @Override
    public NamedSql set(String key, Object value) {
        set(key);
        return setParam(key, value);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public NamedSql setParam(String key, Object value) {
        this.params.put(key, enumCheck(value));
        return this;
    }

    public NamedSql clearParam() {
        this.params.clear();
        return this;
    }

    public NamedSql setParam(Map<String, Object> params) {
        if (MapUtil.isNotEmpty(params)) {
            for (Map.Entry<String,Object> entry: params.entrySet()) {
                this.params.put(entry.getKey(), enumCheck(entry.getValue()));
            }
        }
        return this;
    }

    public NamedSql resetParams(Map<String, Object> params) {
        return clearParam().setParam(params);
    }

    /**
     * 执行当前sql
     *
     * @return 影响的行数
     */
    public int execute() {
        return dba.getNamedParameterJdbcTemplate().update(getSql(), params);
    }

    @Override
    public void query(RowCallbackHandler rch) {
        dba.getNamedParameterJdbcTemplate().query(getSql(), params, rch);
    }

    @Override
    public <T> T queryForObject(RowMapper<T> mapper) throws DataAccessException {
        try {
            return dba.getNamedParameterJdbcTemplate().queryForObject(getSql(), params, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    protected <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        return dba.getNamedParameterJdbcTemplate().query(sql, params, rowMapper);
    }

    @Override
    protected <T> T queryForValue(String sql, Class<T> requiredType) throws DataAccessException {
        return dba.getNamedParameterJdbcTemplate().queryForObject(sql, params, requiredType);
    }

    @Override
    public <T> List<T> queryForValueList(Class<T> elementType) throws DataAccessException {
        return dba.getNamedParameterJdbcTemplate().queryForList(getSql(), params, elementType);
    }

    public int[] batchUpdate(List<Map<String, Object>> data) {
        return dba.getNamedParameterJdbcTemplate().batchUpdate(getSql(), SqlParameterSourceUtils.createBatch(data.toArray()));
    }

}

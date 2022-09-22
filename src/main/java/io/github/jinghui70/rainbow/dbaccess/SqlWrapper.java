package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.utils.StringBuilderWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
@SuppressWarnings("unchecked")
public abstract class SqlWrapper<S extends SqlWrapper<S>> extends StringBuilderWrapper<S> {

    protected Dba dba;

    protected Range<Integer> range; // 查询范围

    private boolean where;

    private boolean set;

    protected SqlWrapper() {
        super();
    }

    public SqlWrapper(String str) {
        super(str);
    }

    public SqlWrapper(Dba dba) {
        this.dba = dba;
    }

    public String getSql() {
        String sql = sb.toString();
        if (range == null) return sql;
        if (range.getFrom() == null) return dba.getDialect().wrapLimitSql(sql, range.getTo());
        return dba.getDialect().wrapRangeSql(sql, range.getFrom(), range.getTo());
    }

    public void setSql(String sql) {
        sb.setLength(0);
        sb.append(sql);
    }

    public abstract S append(Cnd cnd);

    public S from(String table) {
        return append(" FROM ").append(table);
    }

    protected void set() {
        if (set)
            append(",");
        else {
            append(" SET ");
            set = true;
        }
    }

    public abstract S set(String field, Object value);

    public S set(String set) {
        set();
        return append(set);
    }

    protected void where() {
        if (where) {
            append(Cnd.AND);
        } else {
            append(Cnd.WHERE);
            where = true;
        }
    }

    public S where(String cnd) {
        where();
        return append(cnd);
    }

    public S where(Cnd... cnds) {
        for (Cnd cnd : cnds) {
            where();
            append(cnd);
        }
        return (S) this;
    }

    public S where(String field, Object value) {
        return where(new Cnd(field, value));
    }

    public S where(String field, String op, Object value) {
        return where(new Cnd(field, op, value));
    }

    public S and(String cnd) {
        return where(cnd);
    }

    public S and(Cnd cnd) {
        return where(cnd);
    }

    public S and(String field, Object value) {
        return and(new Cnd(field, value));
    }

    public S and(String field, String op, Object value) {
        return and(new Cnd(field, op, value));
    }

    public S or(String cnd) {
        append(Cnd.OR);
        return append(cnd);
    }

    public S or(Cnd cnd) {
        append(Cnd.OR);
        return append(cnd);
    }

    public S or(String field, Object value) {
        return or(new Cnd(field, value));
    }

    public S or(String field, String op, Object value) {
        return or(new Cnd(field, op, value));
    }

    public S orderBy(Collection<String> fields) {
        return append(" ORDER BY ").join(fields);
    }

    public S orderBy(String fields) {
        return append(" ORDER BY ").append(fields);
    }

    public S groupBy(Collection<String> fields) {
        return append(" GROUP BY ").join(fields);
    }

    public S groupBy(String fields) {
        return append(" GROUP BY ").append(fields);
    }

    public abstract void query(RowCallbackHandler rch);

    public abstract <T> T queryForObject(RowMapper<T> mapper) throws DataAccessException;

    protected abstract <T> T queryForValue(String sql, Class<T> requiredType) throws DataAccessException;

    protected abstract <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws DataAccessException;

    public abstract <T> List<T> queryForValueList(Class<T> elementType) throws DataAccessException;

    public Map<String, Object> queryForMap() throws DataAccessException {
        Map<String, Object> result = queryForObject(MapRowMapper.INSTANCE);
        return result == null ? Collections.emptyMap() : result;
    }

    public <T> T queryForObject(Class<T> objectType) throws DataAccessException {
        return queryForObject(BeanMapper.of(objectType));
    }

    public <T> List<T> queryForList(RowMapper<T> rowMapper) throws DataAccessException {
        return queryForList(getSql(), rowMapper);
    }

    public <T> List<T> queryForList(Class<T> objectType) throws DataAccessException {
        return queryForList(BeanMapper.of(objectType));
    }

    /**
     * 查询一个map列表
     *
     * @return Map封装记录的列表
     * @throws DataAccessException 数据处理异常
     */
    public List<Map<String, Object>> queryForList() throws DataAccessException {
        return queryForList(MapRowMapper.INSTANCE);
    }

    public <T> Optional<T> queryForValue(Class<T> requiredType) throws DataAccessException {
        try {
            return Optional.ofNullable(queryForValue(getSql(), requiredType));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public String queryForString() {
        return queryForValue(String.class).orElse(StrUtil.EMPTY);
    }

    public int queryForInt() {
        return queryForValue(Integer.class).orElse(0);
    }

    public double queryForDouble() {
        return queryForValue(Double.class).orElse(0.0);
    }

    public int count() {
        String sql = String.format("SELECT COUNT(1) FROM (%s) C", getSql());
        return queryForValue(sql, int.class);
    }

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, ResultSetFunction<V> valueFunction) {
        Map<K, V> result = new HashMap<>();
        query((rs) -> {
            K key = keyFunc.apply(rs);
            V value = valueFunction.apply(rs);
            result.put(key, value);
        });
        return result;
    }

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, RowMapper<V> rowMapper) {
        Map<K, V> result = new HashMap<>();
        AtomicInteger rowNum = new AtomicInteger(0);
        query((rs) -> {
            K key = keyFunc.apply(rs);
            V value = rowMapper.mapRow(rs, rowNum.getAndIncrement());
            result.put(key, value);
        });
        return result;
    }

    public <K> Map<K, Map<String, Object>> queryToMap(ResultSetFunction<K> keyFunc) {
        return queryToMap(keyFunc, MapRowMapper.INSTANCE);
    }

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, Class<V> clazz) {
        return queryToMap(keyFunc, BeanMapper.of(clazz));
    }

    public <K, T> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc, RowMapper<T> rowMapper) {
        Map<K, List<T>> result = new HashMap<>();
        AtomicInteger rowNum = new AtomicInteger(1);
        query((rs) -> {
            K key = keyFunc.apply(rs);
            List<T> list = result.computeIfAbsent(key, (k) -> new ArrayList<>());
            T value = rowMapper.mapRow(rs, rowNum.getAndIncrement());
            list.add(value);
        });
        return result;
    }

    public <K> Map<K, List<Map<String, Object>>> queryToGroup(ResultSetFunction<K> keyFunc) {
        return queryToGroup(keyFunc, MapRowMapper.INSTANCE);
    }

    public <K, T> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc, Class<T> clazz) {
        return queryToGroup(keyFunc, BeanMapper.of(clazz));
    }

    public <K, T> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc, ResultSetFunction<T> valueFunc) {
        Map<K, List<T>> result = new HashMap<>();
        query((rs) -> {
            K key = keyFunc.apply(rs);
            List<T> list = result.computeIfAbsent(key, (k) -> new ArrayList<>());
            T value = valueFunc.apply(rs);
            list.add(value);
        });
        return result;
    }

    public <T> PageData<T> pageQuery(RowMapper<T> mapper, int pageNo, int pageSize) {
        int count = count();
        if (count == 0 || count <= (pageNo - 1) * pageSize)
            return new PageData<>(count);
        String sql = pageNo == 1 ? dba.getDialect().wrapLimitSql(getSql(), pageSize)
                : dba.getDialect().wrapPagedSql(getSql(), pageNo, pageSize);
        List<T> list = queryForList(sql, mapper);
        return new PageData<>(count, list);
    }

    public <T> PageData<T> pageQuery(Class<T> objectType, int pageNo, int pageSize) {
        return pageQuery(BeanMapper.of(objectType), pageNo, pageSize);
    }

    public PageData<Map<String, Object>> pageQuery(int pageNo, int pageSize) {
        return pageQuery(MapRowMapper.INSTANCE, pageNo, pageSize);
    }

    @Override
    public S limit(int limit) {
        return range(null, limit);
    }

    public S range(Integer from, int to) {
        this.range = Range.of(from, to);
        return (S) this;
    }

}
package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnds;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.EnumMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.map.MapRowMapper;
import io.github.jinghui70.rainbow.dbaccess.mapper.SingleColumnFieldRowMapper;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.utils.StringBuilderWrapper;
import io.github.jinghui70.rainbow.utils.tree.ITreeNode;
import io.github.jinghui70.rainbow.utils.tree.Tree;
import io.github.jinghui70.rainbow.utils.tree.TreeObject;
import io.github.jinghui70.rainbow.utils.tree.WrapTree;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
@SuppressWarnings("unused")
public class Sql extends StringBuilderWrapper<Sql> {

    private final List<Object> params = new ArrayList<>();

    private Dba dba;

    private Range<Integer> range; // 查询范围

    private boolean where;

    private boolean set;

    private boolean countOptimization = true;

    public Sql() {
    }

    public Sql(String str) {
        append(str);
    }

    public Sql(Dba dba) {
        this.dba = dba;
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
    public Sql addParam(Object... params) {
        Collections.addAll(this.params, params);
        return this;
    }

    /**
     * 添加参数
     *
     * @param params 参数列表
     * @return this
     */
    public Sql addParams(List<Object> params) {
        this.params.addAll(params);
        return this;
    }

    /**
     * 重置参数
     *
     * @param params 参数
     * @return this
     */
    public Sql setParam(Object... params) {
        this.params.clear();
        return addParam(params);
    }

    /**
     * 重置参数
     *
     * @param params 参数列表
     * @return this
     */
    public Sql setParams(List<Object> params) {
        this.params.clear();
        return addParams(params);
    }

    public String getSql() {
        String sql = sb.toString();
        if (range == null) return sql;
        if (range.getFrom() == null) return dba.getDialect().wrapLimitSql(sql, range.getTo());
        return dba.getDialect().wrapRangeSql(sql, range.getFrom(), range.getTo());
    }

    public Sql append(Sql sql) {
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return this;
    }

    private void set() {
        if (set)
            append(",");
        else {
            append(" SET ");
            set = true;
        }
    }

    private Sql where(boolean and) {
        if (where) {
            return append(and ? DbaUtil.AND : DbaUtil.OR);
        } else {
            where = true;
            return append(DbaUtil.WHERE);
        }
    }

    public Sql set(String field, Object value) {
        set();
        append(field).append("=?").addParam(enumCheck(value));
        return this;
    }

    /**
     * 拼update语句的一个set部分，fieldMapper将value转换为与之对应的sql参数
     *
     * @param field       字段
     * @param fieldMapper 将value转换为与之对应的sql参数
     * @param value       新值
     * @return 返回自己
     */
    public Sql set(String field, FieldMapper<?> fieldMapper, Object value) {
        return set(field, new FieldValue(value, fieldMapper));
    }

    public Sql set(String setStr) {
        set();
        return append(setStr);
    }

    public Sql set(boolean condition, String field, Object value) {
        return condition ? set(field, value) : this;
    }

    public Sql set(boolean condition, String set) {
        return condition ? set(set) : this;
    }

    public Sql set(boolean condition, String field, FieldMapper<?> fieldMapper, Object value) {
        return condition ? set(field, fieldMapper, value) : this;
    }

    /**
     * 拼 from table
     *
     * @param table 表名
     * @return 返回自己
     */
    public Sql from(String table) {
        return append(" FROM ").append(table);
    }

    public Sql append(Cnd cnd) {
        cnd.toSql(this);
        return this;
    }

    public Sql append(Cnds cnds) {
        cnds.toSql(this);
        return this;
    }

    public Sql where(String str) {
        return where(true).append(str);
    }

    public Sql where(String field, Object value) {
        return where(true, field, value);
    }

    public Sql where(String field, Op op, Object value) {
        return where(true, field, op, value);
    }

    public Sql where(Cnd cnd) {
        return where(true).append(cnd);
    }

    public Sql where(Cnds cnds) {
        if (!cnds.isEmpty()) return where(true).append(cnds);
        return this;
    }

    public Sql where(boolean condition, String str) {
        return condition ? where(true).append(str) : this;
    }

    public Sql where(boolean condition, String field, Object value) {
        if (Op.IS_NULL.equals(value) || Op.IS_NOT_NULL.equals(value)) {
            return where(condition, field, (Op) value, null);
        }
        return where(condition, field, Op.EQ, value);
    }

    public Sql where(boolean condition, String field, Op op, Object value) {
        return condition ? where(new Cnd(field, op, value)) : this;
    }

    public Sql where(boolean condition, Supplier<Cnds> supplier) {
        if (condition) {
            Cnds cnds = supplier.get();
            if (!cnds.isEmpty())
                return where(true).append(cnds);
        }
        return this;
    }

    public Sql and(String str) {
        return where(str);
    }

    public Sql and(String field, Object value) {
        return where(true, field, value);
    }

    public Sql and(String field, Op op, Object value) {
        return where(true, field, op, value);
    }

    public Sql and(Cnd cnd) {
        return where(cnd);
    }

    public Sql and(Cnds cnds) {
        return where(cnds);
    }

    public Sql and(boolean condition, String str) {
        return where(condition, str);
    }

    public Sql and(boolean condition, String field, Object value) {
        return where(condition, field, value);
    }

    public Sql and(boolean condition, String field, Op op, Object value) {
        return where(condition, field, op, value);
    }

    public Sql and(boolean condition, Supplier<Cnds> supplier) {
        return where(condition, supplier);
    }

    public Sql or(String str) {
        return where(false).append(str);
    }

    public Sql or(String field, Object value) {
        return or(true, field, value);
    }

    public Sql or(String field, Op op, Object value) {
        return or(true, field, op, value);
    }

    public Sql or(Cnd cnd) {
        return or(true, cnd);
    }

    public Sql or(Cnds cnds) {
        return cnds.isEmpty() ? this : where(false).append(cnds);
    }

    public Sql or(boolean condition, String field, Object value) {
        if (Op.IS_NULL.equals(value) || Op.IS_NOT_NULL.equals(value)) {
            return or(condition, field, (Op) value, null);
        }
        return or(condition, new Cnd(field, Op.EQ, value));
    }

    public Sql or(boolean condition, String field, Op op, Object value) {
        return or(condition, new Cnd(field, op, value));
    }

    public Sql or(boolean condition, Cnd cnd) {
        return condition ? where(false).append(cnd) : this;
    }

    public Sql or(boolean condition, Supplier<Cnds> supplier) {
        if (condition) {
            Cnds cnds = supplier.get();
            if (!cnds.isEmpty())
                return or(cnds);
        }
        return this;
    }

    public Sql orderBy(String fields) {
        return (StrUtil.isNotBlank(fields))
                ? append(DbaUtil.ORDER_BY).append(fields)
                : this;
    }

    public Sql orderBy(Collection<OrderBy> orderBys) {
        return (CollUtil.isNotEmpty(orderBys))
                ? append(DbaUtil.ORDER_BY).join(orderBys)
                : this;
    }

    public Sql groupBy(String fields) {
        return append(DbaUtil.GROUP_BY).append(fields);
    }

    /**
     * 执行当前sql
     *
     * @return 执行影响的行数
     */
    public int execute() {
        if (params.isEmpty())
            return dba.getJdbcTemplate().update(getSql());
        else
            return dba.getJdbcTemplate().update(getSql(), new ArgumentSetter(params));
    }

    public int[] batchUpdate(List<Object[]> batchArgs) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return dba.getJdbcTemplate().batchUpdate(
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
        return dba.getJdbcTemplate().batchUpdate(getSql(), batchArgs, batchSize,
                (ps, argument) -> {
                    int colIndex = 1;
                    for (Object value : argument) {
                        DbaUtil.setParameterValue(ps, colIndex++, value, nullTypeCache);
                    }
                });
    }


    public <T> T queryForObject(RowMapper<T> mapper) {
        try {
            if (params.isEmpty())
                return dba.getJdbcTemplate().queryForObject(getSql(), mapper);
            else
                return dba.getJdbcTemplate().queryForObject(getSql(), mapper, params.toArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> Optional<T> queryForObjectOptional(RowMapper<T> mapper) {
        return Optional.ofNullable(queryForObject(mapper));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> RowMapper<T> typeToMapper(Class<T> requiredType) {
        return requiredType.isEnum()
                ? new SingleColumnFieldRowMapper(new EnumMapper(requiredType))
                : new SingleColumnRowMapper<>(requiredType);
    }

    public <T> T queryForValue(Class<T> requiredType) {
        return queryForObject(typeToMapper(requiredType));
    }

    public <T> Optional<T> queryForValueOptional(Class<T> requiredType) {
        return Optional.ofNullable(queryForValue(requiredType));
    }

    public <T> T queryForValue(FieldMapper<T> mapper) {
        return queryForObject(new SingleColumnFieldRowMapper<>(mapper));
    }

    public <T> Optional<T> queryForValueOptional(FieldMapper<T> mapper) {
        return Optional.ofNullable(queryForValue(mapper));
    }

    public String queryForString() {
        return queryForValueOptional(String.class).orElse(StrUtil.EMPTY);
    }

    public Optional<String> queryForStringOptional() {
        return queryForValueOptional(String.class);
    }

    public int queryForInt() {
        return queryForValueOptional(Integer.class).orElse(0);
    }

    public Optional<Integer> queryForIntOptional() {
        return queryForValueOptional(Integer.class);
    }

    public double queryForDouble() {
        return queryForValueOptional(Double.class).orElse(0.0);
    }

    public Optional<Double> queryForDoubleOptional() {
        return queryForValueOptional(Double.class);
    }

    public LocalDate queryForDate() {
        return queryForValue(LocalDate.class);
    }

    public Optional<LocalDate> queryForDateOptional() {
        return queryForValueOptional(LocalDate.class);
    }

    public <T> T queryForObject(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValue(objectType);
        return queryForObject(BeanMapper.of(objectType));
    }

    public <T> Optional<T> queryForObjectOptional(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValueOptional(objectType);
        return queryForObjectOptional(BeanMapper.of(objectType));
    }

    public Map<String, Object> queryForMap() {
        Map<String, Object> result = queryForObject(MapRowMapper.INSTANCE);
        return result == null ? Collections.emptyMap() : result;
    }

    public void query(RowCallbackHandler rch) {
        if (params.isEmpty())
            dba.getJdbcTemplate().query(getSql(), rch);
        else
            dba.getJdbcTemplate().query(getSql(), new ArgumentSetter(params), rch);
    }

    private <T> T query(String sql, ResultSetExtractor<T> rse) {
        return (params.isEmpty())
                ? dba.getJdbcTemplate().query(sql, rse)
                : dba.getJdbcTemplate().query(sql, new ArgumentSetter(params), rse);
    }

    private <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        ResultSetExtractor<List<T>> rse = new RowMapperResultSetExtractor<>(rowMapper);
        return query(sql, rse);
    }

    public <T> List<T> queryForList(RowMapper<T> rowMapper) {
        return queryForList(getSql(), rowMapper);
    }

    public List<Map<String, Object>> queryForList() {
        return queryForList(MapRowMapper.INSTANCE);
    }

    public <T> List<T> queryForList(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForList(typeToMapper(objectType));
        return queryForList(BeanMapper.of(objectType));
    }

    public <T> List<T> queryForList(FieldMapper<T> fieldMapper) throws DataAccessException {
        return queryForList(new SingleColumnFieldRowMapper<>(fieldMapper));
    }

    public int count() {
        String sql = getSql().toUpperCase();
        if (!countOptimization || sql.contains("DISTINCT") || sql.contains(DbaUtil.GROUP_BY) || sql.contains(" UNION ")) {
            sql = String.format("SELECT COUNT(*) FROM (%s) C", sql);
        } else {
            int orderBy = sql.lastIndexOf(DbaUtil.ORDER_BY);
            sql = "select count(*) " + sql.substring(sql.indexOf("FROM"), orderBy > 0 ? orderBy : sql.length());
        }
        Integer result = dba.getJdbcTemplate().queryForObject(sql, Integer.class, params.toArray());
        return result == null ? 0 : result;
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

    public PageData<Map<String, Object>> pageQuery(int pageNo, int pageSize) {
        return pageQuery(MapRowMapper.INSTANCE, pageNo, pageSize);
    }

    public <T> PageData<T> pageQuery(Class<T> objectType, int pageNo, int pageSize) {
        if (BeanUtils.isSimpleValueType(objectType)) {
            int count = count();
            if (count == 0 || count <= (pageNo - 1) * pageSize)
                return new PageData<>(count);
            String sql = pageNo == 1 ? dba.getDialect().wrapLimitSql(getSql(), pageSize)
                    : dba.getDialect().wrapPagedSql(getSql(), pageNo, pageSize);
            List<T> list = queryForList(sql, typeToMapper(objectType));
            return new PageData<>(count, list);
        }
        return pageQuery(BeanMapper.of(objectType), pageNo, pageSize);
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

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, RowMapper<V> rowMapper, Supplier<Map<K, V>> supplier) {
        Map<K, V> result = supplier.get();
        AtomicInteger rowNum = new AtomicInteger(0);
        query((rs) -> {
            K key = keyFunc.apply(rs);
            V value = rowMapper.mapRow(rs, rowNum.getAndIncrement());
            result.put(key, value);
        });
        return result;
    }

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, RowMapper<V> rowMapper) {
        return queryToMap(keyFunc, rowMapper, HashMap::new);
    }


    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, Class<V> clazz, Supplier<Map<K, V>> supplier) {
        return queryToMap(keyFunc, BeanMapper.of(clazz), supplier);
    }

    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, Class<V> clazz) {
        return queryToMap(keyFunc, BeanMapper.of(clazz));
    }


    public <K> Map<K, Map<String, Object>> queryToMap(ResultSetFunction<K> keyFunc) {
        return queryToMap(keyFunc, MapRowMapper.INSTANCE);
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

    public <K> Map<K, List<Map<String, Object>>> queryToGroup(ResultSetFunction<K> keyFunc) {
        return queryToGroup(keyFunc, MapRowMapper.INSTANCE);
    }


    public <T extends ITreeNode<T>> Tree<T> queryForTree(Class<T> objectType) {
        return queryForTree(BeanMapper.of(objectType));
    }

    public <T extends ITreeNode<T>> Tree<T> queryForTree(RowMapper<T> mapper) {
        List<T> result = new ArrayList<>();
        Map<String, String> parentIdMap = new LinkedHashMap<>();
        Map<String, T> itemMap = new HashMap<>();
        AtomicInteger row = new AtomicInteger(1);
        query(rs -> {
            String pid = rs.getString("PID");
            String id = rs.getString("ID");
            parentIdMap.put(id, pid);
            T item = mapper.mapRow(rs, row.getAndIncrement());
            itemMap.put(id, item);
        });
        for (Map.Entry<String, String> entry : parentIdMap.entrySet()) {
            String id = entry.getKey();
            String pid = entry.getValue();
            T item = itemMap.get(id);
            T parent = itemMap.get(pid);
            if (parent == null) {
                result.add(item);
            } else
                parent.addChild(item);
        }
        return new Tree<>(result, itemMap);
    }

    public <T> WrapTree<T> queryForWrapTree(Class<T> objectType) {
        return queryForWrapTree(BeanMapper.of(objectType));
    }

    public <T> WrapTree<T> queryForWrapTree(RowMapper<T> mapper) {
        List<TreeObject<T>> result = new ArrayList<>();
        Map<String, String> parentIdMap = new LinkedHashMap<>();
        Map<String, TreeObject<T>> itemMap = new HashMap<>();
        AtomicInteger row = new AtomicInteger(1);
        query(rs -> {
            String pid = rs.getString("PID");
            String id = rs.getString("ID");
            parentIdMap.put(id, pid);
            T item = mapper.mapRow(rs, row.getAndIncrement());
            itemMap.put(id, new TreeObject<>(item));
        });
        for (Map.Entry<String, String> entry : parentIdMap.entrySet()) {
            String id = entry.getKey();
            String pid = entry.getValue();
            TreeObject<T> item = itemMap.get(id);
            TreeObject<T> parent = itemMap.get(pid);
            if (parent == null) {
                result.add(item);
            } else
                parent.addChild(item);
        }
        return new WrapTree<>(result, itemMap);
    }

    public Sql disableCountOptimization() {
        this.countOptimization = false;
        return this;
    }

    public boolean exist() {
        String sql = getSql().toUpperCase();
        if (!countOptimization || sql.contains("DISTINCT") || sql.contains(DbaUtil.GROUP_BY) || sql.contains(" UNION ")) {
            sql = String.format("SELECT 1 FROM (%s LIMIT 1) C", sql);
        } else {
            sql = "select 1 " + sql.substring(sql.indexOf("FROM"));
            sql = dba.getDialect().wrapLimitSql(sql, 1);
        }
        try {
            dba.getJdbcTemplate().queryForObject(sql, Integer.class, params.toArray());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Sql limit(int limit) {
        return range(null, limit);
    }

    public Sql range(Integer from, int to) {
        this.range = Range.of(from, to);
        return this;
    }
}
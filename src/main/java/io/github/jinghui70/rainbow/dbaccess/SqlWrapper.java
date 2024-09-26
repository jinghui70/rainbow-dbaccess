package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.EnumMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.mapper.MapRowMapper;
import io.github.jinghui70.rainbow.dbaccess.mapper.SingleColumnFieldRowMapper;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.utils.StringBuilderWrapper;
import io.github.jinghui70.rainbow.utils.TreeNode;
import io.github.jinghui70.rainbow.utils.WrapTreeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
@SuppressWarnings({"unchecked"})
public abstract class SqlWrapper<S extends SqlWrapper<S>> extends StringBuilderWrapper<S> {

    protected Dba dba;

    protected Range<Integer> range; // 查询范围

    private boolean where;

    private boolean set;

    protected SqlWrapper() {
        super();
    }

    protected SqlWrapper(String str) {
        super(str);
    }

    protected SqlWrapper(Dba dba) {
        this.dba = dba;
    }

    /**
     * 获取当前拼的sql内容
     *
     * @return sql
     */
    public String getSql() {
        String sql = sb.toString();
        if (range == null) return sql;
        if (range.getFrom() == null) return dba.getDialect().wrapLimitSql(sql, range.getTo());
        return dba.getDialect().wrapRangeSql(sql, range.getFrom(), range.getTo());
    }

    /**
     * 复用当前对象，重新设置sql内容
     *
     * @param sql 新的sql
     */
    public void setSql(String sql) {
        sb.setLength(0);
        sb.append(sql);
    }

    /**
     * 添加一个条件
     *
     * @param cnd 条件对象
     * @return 返回自己
     */
    public abstract S append(Cnd cnd);

    /**
     * 拼 from table
     *
     * @param table 表名
     * @return 返回自己
     */
    public S from(String table) {
        return append(" FROM ").append(table);
    }

    /**
     * 判断是不是第一个set
     */
    protected void set() {
        if (set)
            append(",");
        else {
            append(" SET ");
            set = true;
        }
    }

    /**
     * 拼update语句的一个set部分
     *
     * @param field 字段
     * @param value 新值
     * @return 返回自己
     */
    public abstract S set(String field, Object value);

    /**
     * 拼update语句的一个set部分，用于上一个方法不方便的时候。比如 set a = a + ?, 或者 a=a+:deltaA
     *
     * @param set 需要set的内容
     * @return 返回自己
     */
    public S set(String set) {
        set();
        return append(set);
    }

    /**
     * 判断是不是第一个where
     */
    protected void where() {
        if (where) {
            append(Cnd.AND);
        } else {
            append(Cnd.WHERE);
            where = true;
        }
    }

    /**
     * 添加一个条件，需要自己处理条件内容，比如 where("a=1")或者where("a=?").addParam(1)
     *
     * @param cnd 条件字符串
     * @return 返回自己
     */
    public S where(String cnd) {
        where();
        return append(cnd);
    }

    /**
     * 添加条件列表
     *
     * @param cnds 条件列表
     * @return 返回自己
     */
    public S where(Collection<Cnd> cnds) {
        if (CollUtil.isNotEmpty(cnds))
            for (Cnd cnd : cnds) {
                where();
                append(cnd);
            }
        return (S) this;
    }

    public S where(Cnd cnd) {
        where();
        append(cnd);
        return (S) this;
    }

    public S where(String field, Object value) {
        return where(true, field, Op.EQ, value);
    }

    public S where(boolean condition, String field, Object value) {
        return where(condition, field, Op.EQ, value);
    }

    @Deprecated
    public S where(String field, String op, Object value) {
        return where(new Cnd(field, op, value));
    }

    public S where(String field, Op op, Object value) {
        return where(true, field, op, value);
    }

    public S where(boolean condition, String field, Op op, Object value) {
        if (condition) where(new Cnd(field, op, value));
        return (S) this;
    }

    /**
     * AND + 添加条件列表
     *
     * @param cnds 条件列表
     * @return 返回自己
     */
    public S and(Collection<Cnd> cnds) {
        return where(cnds);
    }

    /**
     * 添加一个And条件
     *
     * @param cnd 条件字符串
     * @return 返回自己
     */
    public S and(String cnd) {
        return where(cnd);
    }

    public S and(Cnd cnd) {
        return where(cnd);
    }

    public S and(String field, Object value) {
        return where(true, field, Op.EQ, value);
    }

    public S and(boolean condition, String field, Object value) {
        return where(condition, field, Op.EQ, value);
    }

    @Deprecated
    public S and(String field, String op, Object value) {
        return where(field, op, value);
    }

    public S and(String field, Op op, Object value) {
        return where(true, field, op, value);
    }

    public S and(boolean condition, String field, Op op, Object value) {
        return where(condition, field, op, value);
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
        return or(true, field, Op.EQ, value);
    }

    public S or(boolean condition, String field, Object value) {
        return or(condition, field, Op.EQ, value);
    }

    @Deprecated
    public S or(String field, String op, Object value) {
        return or(new Cnd(field, op, value));
    }

    public S or(String field, Op op, Object value) {
        return or(true, field, op, value);
    }

    public S or(boolean condition, String field, Op op, Object value) {
        if (condition) {
            or(new Cnd(field, op, value));
        }
        return (S) this;
    }

    public S orderBy(String fields) {
        if (StrUtil.isNotBlank(fields))
            return append(" ORDER BY ").append(fields);
        return (S) this;
    }

    public S groupBy(String fields) {
        return append(" GROUP BY ").append(fields);
    }

    /**
     * 查询并按回调函数进行结果处理
     *
     * @param rch 处理每行数据的回调函数
     */
    public abstract void query(RowCallbackHandler rch);

    /**
     * 查询一条记录的一个字段，由派生类实现。
     *
     * @param requiredType 数据类型
     * @param <T>          数据泛型
     * @return 查询结果
     * @throws DataAccessException 底层异常
     */
    @SuppressWarnings("rawtypes")
    private <T> RowMapper<T> typeToMapper(Class<T> requiredType) {
        return requiredType.isEnum()
                ? new SingleColumnFieldRowMapper(new EnumMapper(requiredType))
                : new SingleColumnRowMapper<>(requiredType);
    }

    public <T> T queryForValue(Class<T> requiredType) throws DataAccessException {
        return queryForObject(typeToMapper(requiredType));
    }

    public <T> Optional<T> queryForValueOptional(Class<T> requiredType) throws DataAccessException {
        try {
            return Optional.ofNullable(queryForValue(requiredType));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public <T> T queryForValue(FieldMapper<T> mapper) throws DataAccessException {
        return queryForObject(new SingleColumnFieldRowMapper<T>(mapper));
    }

    public <T> Optional<T> queryForValueOptional(FieldMapper<T> mapper) throws DataAccessException {
        try {
            return Optional.ofNullable(queryForValue(mapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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

    protected abstract <T> T queryForObject(String sql, RowMapper<T> mapper) throws DataAccessException;

    public <T> T queryForObject(RowMapper<T> mapper) throws DataAccessException {
        return queryForObject(getSql(), mapper);
    }

    public <T> Optional<T> queryForObjectOptional(RowMapper<T> mapper) throws DataAccessException {
        try {
            return Optional.ofNullable(queryForObject(mapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public <T> T queryForObject(Class<T> objectType) throws DataAccessException {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValue(objectType);
        return queryForObject(BeanMapper.of(objectType));
    }

    public <T> Optional<T> queryForObjectOptional(Class<T> objectType) throws DataAccessException {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValueOptional(objectType);
        return queryForObjectOptional(BeanMapper.of(objectType));
    }

    public Map<String, Object> queryForMap() throws DataAccessException {
        Map<String, Object> result = queryForObject(MapRowMapper.INSTANCE);
        return result == null ? Collections.emptyMap() : result;
    }

    /**
     * 获取对象列表
     *
     * @param rowMapper 对象Mapper
     * @param <T>       对象泛型
     * @return 结果列表
     * @throws DataAccessException 数据底层异常
     */
    protected abstract <T> List<T> queryForList(String sql, RowMapper<T> rowMapper);

    public <T> List<T> queryForList(RowMapper<T> rowMapper) {
        return queryForList(getSql(), rowMapper);
    }

    /**
     * 查询一个对象列表
     *
     * @param objectType 对象类型
     * @param <T>        类型泛型
     * @return 结果列表
     * @throws DataAccessException 数据底层异常
     */
    public <T> List<T> queryForList(Class<T> objectType) throws DataAccessException {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForList(typeToMapper(objectType));
        return queryForList(BeanMapper.of(objectType));
    }

    public <T> List<T> queryForList(FieldMapper<T> fieldMapper) throws DataAccessException {
        return queryForList(new SingleColumnFieldRowMapper<>(fieldMapper));
    }

    public int count() {
        String sql = getSql().toLowerCase();
        if (sql.contains("distinct") || sql.contains("group by") || sql.contains("union")) {
            sql = String.format("SELECT COUNT(*) FROM (%s) C", sql);
        } else {
            int orderBy = sql.lastIndexOf(" order by");
            sql = "select count(1) " + sql.substring(sql.indexOf("from"), orderBy > 0 ? orderBy : sql.length());
        }
        return queryForObject(sql, new SingleColumnRowMapper<>(Integer.class));
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

    public <T extends TreeNode<T>> List<T> queryForTree(Class<T> objectType) {
        return queryForTree(BeanMapper.of(objectType));
    }

    public <T> List<WrapTreeNode<T>> queryForWrapTree(Class<T> objectType) {
        return queryForWrapTree(BeanMapper.of(objectType));
    }

    public <T> List<WrapTreeNode<T>> queryForWrapTree(RowMapper<T> mapper) {
        return queryForTree((rs, rowNum) -> new WrapTreeNode<>(mapper.mapRow(rs, rowNum)));
    }

    public <T extends TreeNode<T>> List<T> queryForTree(RowMapper<T> mapper) {
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
        return result;
    }

    public List<Map<String, Object>> queryForTree(MapRowMapper mapper) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> parentIdMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> itemMap = new HashMap<>();
        AtomicInteger row = new AtomicInteger(1);
        query(rs -> {
            String pid = rs.getString("PID");
            String id = rs.getString("ID");
            parentIdMap.put(id, pid);
            Map<String, Object> item = mapper.mapRow(rs, row.getAndIncrement());
            itemMap.put(id, item);
        });
        for (Map.Entry<String, String> entry : parentIdMap.entrySet()) {
            String id = entry.getKey();
            String pid = entry.getValue();
            Map<String, Object> item = itemMap.get(id);
            Map<String, Object> parent = itemMap.get(pid);
            if (parent == null) {
                result.add(item);
            } else {
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.computeIfAbsent("children",
                        k -> new ArrayList<Map<String, Object>>());
                children.add(item);
            }
        }
        return result;
    }

    @Override
    public S limit(int limit) {
        return range(null, limit);
    }

    public S range(Integer from, int to) {
        this.range = Range.of(from, to);
        return (S) this;
    }

    public abstract int execute();

}
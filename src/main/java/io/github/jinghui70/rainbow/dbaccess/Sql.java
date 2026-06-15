package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.EnumFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.MapRowMapper;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.SingleColumnFieldRowMapper;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.utils.StringBuilderWrapper;
import io.github.jinghui70.rainbow.utils.tree.ITreeNode;
import io.github.jinghui70.rainbow.utils.tree.Tree;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.SELECT;

/**
 * SQL 语句构建与执行对象，继承自 {@link StringBuilderWrapper}，支持链式调用。
 * <p>
 * {@code Sql} 是 rainbow-dbaccess 的核心类之一，用于以流畅的 API 构建 SQL 语句并执行查询。
 * 它将 SQL 字符串拼接与参数管理封装在一起，自动处理参数绑定，避免手动拼接带来的 SQL 注入风险。
 * <p>
 * <p>
 * 典型用法示例：
 * <pre>{@code
 * // 构建并执行一个简单查询
 * List<User> users = Sql.select().from("T_USER").where("AGE", Op.GT, 18).queryForList(User.class);
 *
 * // 链式构建复杂查询
 * List<User> users = Sql.select("NAME", "AGE")
 *     .from(User.class)
 *     .where("STATUS", Status.ACTIVE)
 *     .and(
 *        Cnd.or(
 *          Cnd.and(Cnd.where("GENDER", Gender.Male), Cnd.where("AGE", Op.GE, 20)),
 *          Cnd.and(Cnd.where("GENDER", Gender.Female), Cnd.where("AGE", Op.GE, 18)),
 *        )
 *      )
 *     .orderBy("NAME")
 *     .queryForList(User.class);
 *
 * // 分页查询
 * PageData<User> page = Sql.select().from(User.class).pageQuery(User.class, 1, 10);
 * }</pre>
 *
 * @author lijinghui
 * @see Dba
 * @see Cnd
 */
public class Sql extends StringBuilderWrapper<Sql> {

    private final List<Object> params = new ArrayList<>();

    private Dba dba;

    private Range<Integer> range; // 查询范围

    private boolean where;

    private boolean countOptimization = true;

    /**
     * 默认构造函数，创建一个空的 Sql 对象。
     */
    public Sql() {
    }

    /**
     * 使用初始 SQL 片段构造 Sql 对象。
     *
     * @param str 初始 SQL 片段
     */
    public Sql(String str) {
        append(str);
    }

    /**
     * 使用指定的 Dba 构造 Sql 对象，后续可直接执行查询。
     *
     * @param dba 数据库访问对象
     */
    public Sql(Dba dba) {
        this.dba = dba;
    }

    /**
     * 设置关联的 Dba 对象，用于后续执行查询。
     *
     * @param dba 数据库访问对象
     * @return this
     */
    public Sql setDba(Dba dba) {
        this.dba = dba;
        return this;
    }

    /**
     * 获取当前 Sql 的参数列表。
     *
     * @return 参数列表
     */
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

    /**
     * 获取最终的 SQL 语句字符串。
     * <p>如果设置了查询范围（{@link #limit} 或 {@link #range}），会自动通过数据库方言包装分页 SQL。
     *
     * @return 完整的 SQL 语句
     */
    public String getSql() {
        String sql = sb.toString();
        if (range == null) return sql;
        if (range.getFrom() == null) return dba.getDialect().wrapLimitSql(sql, range.getTo());
        return dba.getDialect().wrapRangeSql(sql, range.getFrom(), range.getTo());
    }

    /**
     * 追加另一个 Sql 对象的内容，包括 SQL 片段和参数。
     *
     * @param sql 要追加的 Sql 对象
     * @return this
     */
    public Sql append(Sql sql) {
        append(sql.getSql());
        this.params.addAll(sql.getParams());
        return this;
    }

    private Sql where(boolean and) {
        if (where) {
            return append(and ? DbaUtil.AND : DbaUtil.OR);
        } else {
            where = true;
            return append(DbaUtil.WHERE);
        }
    }

    /**
     * 拼接 FROM 子句，指定查询的表名。
     *
     * @param table 表名
     * @return this
     */
    public Sql from(String table) {
        return append(" FROM ").append(table);
    }

    /**
     * 拼接 FROM 子句，根据实体类的 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Table} 注解自动获取表名。
     *
     * @param entityClass 实体类
     * @return this
     */
    public Sql from(Class<?> entityClass) {
        return from(DbaUtil.tableName(entityClass));
    }

    /**
     * 追加一个条件对象到当前 SQL 中。
     *
     * @param cnd 条件对象
     * @return this
     * @see Cnd
     */
    public Sql append(Cnd cnd) {
        cnd.toSql(this);
        return this;
    }

    /**
     * 拼接 WHERE 条件子句（AND 连接）。使用原始 SQL 片段作为条件。
     * <p>首次调用时生成 WHERE 关键字，后续调用自动用 AND 连接。
     *
     * @param str SQL 条件片段
     * @return this
     */
    public Sql where(String str) {
        return where(true).append(str);
    }

    /**
     * 拼接 WHERE 条件子句（AND 连接），使用等值条件（字段 = 值）。
     *
     * @param field 字段名
     * @param value 条件值
     * @return this
     */
    public Sql where(String field, Object value) {
        return where(Cnd.where(field, value));
    }

    /**
     * 拼接 WHERE 条件子句（AND 连接），使用指定比较运算符。
     *
     * @param field 字段名
     * @param op    比较运算符
     * @param value 条件值
     * @return this
     * @see Op
     */
    public Sql where(String field, Op op, Object value) {
        return where(Cnd.where(field, op, value));
    }

    /**
     * 拼接 WHERE 条件子句（AND 连接），使用条件对象。
     * <p>若 cnd 为 null 则不追加任何内容。
     *
     * @param cnd 条件对象，可以为 null
     * @return this
     */
    public Sql where(Cnd cnd) {
        return cnd == null ? this : where(true).append(cnd);
    }

    /**
     * 条件拼接 WHERE 子句，仅当 condition 为 true 时才拼接。
     *
     * @param condition 是否拼接条件
     * @param supplier  条件对象提供者，仅当 condition 为 true 时调用
     * @return this
     */
    public Sql where(boolean condition, Supplier<Cnd> supplier) {
        return condition ? where(supplier.get()) : this;
    }

    /**
     * 条件拼接 WHERE 子句，仅当 condition 为 true 时才拼接原始 SQL 片段。
     *
     * @param condition 是否拼接条件
     * @param str       SQL 条件片段
     * @return this
     */
    public Sql where(boolean condition, String str) {
        return condition ? where(str) : this;
    }

    /**
     * 条件拼接 WHERE 子句，仅当 condition 为 true 时才拼接等值条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param value     条件值
     * @return this
     */
    public Sql where(boolean condition, String field, Object value) {
        return condition ? where(field, value) : this;
    }

    /**
     * 条件拼接 WHERE 子句，仅当 condition 为 true 时才拼接指定运算符的条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param op        比较运算符
     * @param value     条件值
     * @return this
     */
    public Sql where(boolean condition, String field, Op op, Object value) {
        return condition ? where(field, op, value) : this;
    }

    /**
     * 拼接 WHERE 条件子句，遍历条件列表依次用 AND 连接。
     *
     * @param cnds 条件列表
     * @return this
     */
    public Sql where(List<Cnd> cnds) {
        if (CollUtil.isNotEmpty(cnds))
            for (Cnd cnd : cnds)
                where(cnd);
        return this;
    }

    /**
     * 拼接 AND 条件，使用原始 SQL 片段。等价于 {@code where(str)}。
     *
     * @param str SQL 条件片段
     * @return this
     */
    public Sql and(String str) {
        return where(str);
    }

    /**
     * 拼接 AND 等值条件（字段 = 值）。等价于 {@code where(field, value)}。
     *
     * @param field 字段名
     * @param value 条件值
     * @return this
     */
    public Sql and(String field, Object value) {
        return where(field, value);
    }

    /**
     * 拼接 AND 条件，使用指定比较运算符。等价于 {@code where(field, op, value)}。
     *
     * @param field 字段名
     * @param op    比较运算符
     * @param value 条件值
     * @return this
     */
    public Sql and(String field, Op op, Object value) {
        return where(field, op, value);
    }

    /**
     * 拼接 AND 条件对象。等价于 {@code where(cnd)}。
     *
     * @param cnd 条件对象
     * @return this
     */
    public Sql and(Cnd cnd) {
        return where(cnd);
    }

    /**
     * 条件拼接 AND 子句，仅当 condition 为 true 时才拼接原始 SQL 片段。
     *
     * @param condition 是否拼接条件
     * @param str       SQL 条件片段
     * @return this
     */
    public Sql and(boolean condition, String str) {
        return where(condition, str);
    }

    /**
     * 条件拼接 AND 子句，仅当 condition 为 true 时才拼接等值条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param value     条件值
     * @return this
     */
    public Sql and(boolean condition, String field, Object value) {
        return where(condition, field, value);
    }

    /**
     * 条件拼接 AND 子句，仅当 condition 为 true 时才拼接指定运算符的条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param op        比较运算符
     * @param value     条件值
     * @return this
     */
    public Sql and(boolean condition, String field, Op op, Object value) {
        return where(condition, field, op, value);
    }

    /**
     * 条件拼接 AND 子句，仅当 condition 为 true 时才拼接条件对象。
     *
     * @param condition 是否拼接条件
     * @param supplier  条件对象提供者，仅当 condition 为 true 时调用
     * @return this
     */
    public Sql and(boolean condition, Supplier<Cnd> supplier) {
        return where(condition, supplier);
    }

    /**
     * 拼接 OR 条件，使用原始 SQL 片段。
     *
     * @param str SQL 条件片段
     * @return this
     */
    public Sql or(String str) {
        return where(false).append(str);
    }

    /**
     * 拼接 OR 等值条件（字段 = 值）。
     *
     * @param field 字段名
     * @param value 条件值
     * @return this
     */
    public Sql or(String field, Object value) {
        return or(Cnd.where(field, value));
    }

    /**
     * 拼接 OR 条件，使用指定比较运算符。
     *
     * @param field 字段名
     * @param op    比较运算符
     * @param value 条件值
     * @return this
     */
    public Sql or(String field, Op op, Object value) {
        return or(Cnd.where(field, op, value));
    }

    /**
     * 拼接 OR 条件对象。若 cnd 为 null 则不追加任何内容。
     *
     * @param cnd 条件对象，可以为 null
     * @return this
     */
    public Sql or(Cnd cnd) {
        return cnd == null ? this : where(false).append(cnd);
    }

    /**
     * 条件拼接 OR 子句，仅当 condition 为 true 时才拼接等值条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param value     条件值
     * @return this
     */
    public Sql or(boolean condition, String field, Object value) {
        return condition ? or(field, value) : this;
    }

    /**
     * 条件拼接 OR 子句，仅当 condition 为 true 时才拼接指定运算符的条件。
     *
     * @param condition 是否拼接条件
     * @param field     字段名
     * @param op        比较运算符
     * @param value     条件值
     * @return this
     */
    public Sql or(boolean condition, String field, Op op, Object value) {
        return condition ? or(field, op, value) : this;
    }

    /**
     * 条件拼接 OR 子句，仅当 condition 为 true 时才拼接条件对象。
     *
     * @param condition 是否拼接条件
     * @param supplier  条件对象提供者，仅当 condition 为 true 时调用
     * @return this
     */
    public Sql or(boolean condition, Supplier<Cnd> supplier) {
        return condition ? or(supplier.get()) : this;
    }

    /**
     * 拼接 ORDER BY 子句，按指定字段升序排序。
     *
     * @param fields 排序字段名，支持 "FIELD ASC"、"FIELD DESC" 格式
     * @return this
     */
    public Sql orderBy(String... fields) {
        return ArrayUtil.isNotEmpty(fields)
                ? append(DbaUtil.ORDER_BY).join(fields)
                : this;
    }

    /**
     * 拼接 ORDER BY 子句，使用 {@link OrderBy} 对象列表指定排序方式。
     *
     * @param orderBys 排序对象列表
     * @return this
     * @see OrderBy
     */
    public Sql orderBy(List<OrderBy> orderBys) {
        return (CollUtil.isNotEmpty(orderBys))
                ? append(DbaUtil.ORDER_BY).join(orderBys)
                : this;
    }

    /**
     * 拼接 GROUP BY 子句。
     *
     * @param fields 分组字段名
     * @return this
     */
    public Sql groupBy(String... fields) {
        return append(DbaUtil.GROUP_BY).join(fields);
    }

    /**
     * 执行当前 SQL 语句（INSERT/UPDATE/DELETE），自动处理参数绑定。
     *
     * @return 受影响的行数
     */
    public int execute() {
        if (params.isEmpty())
            return dba.getJdbcTemplate().update(getSql());
        else
            return dba.getJdbcTemplate().update(getSql(), new ArgumentSetter(params));
    }

    /**
     * 批量执行 SQL 语句，一次性提交所有参数。
     *
     * @param batchArgs 批量参数列表，每个元素为一组参数
     * @return 每次执行受影响的行数数组
     */
    public int[] batchUpdate(List<Object[]> batchArgs) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        return dba.getJdbcTemplate().batchUpdate(
                getSql(),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
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

    /**
     * 分批批量执行 SQL 语句，每批执行指定数量的参数。
     *
     * @param batchArgs 批量参数列表，每个元素为一组参数
     * @param batchSize 每批执行的参数组数
     * @return 每批每次执行受影响的行数二维数组
     */
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


    /**
     * 查询单条记录，使用自定义 RowMapper 映射结果。
     * <p>若无匹配记录返回 null，不会抛出 {@link EmptyResultDataAccessException}。
     *
     * @param mapper 行映射器
     * @param <T>    结果类型
     * @return 查询结果，无数据时返回 null
     */
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

    /**
     * 查询单条记录，使用自定义 RowMapper 映射结果，以 {@link Optional} 包装返回。
     *
     * @param mapper 行映射器
     * @param <T>    结果类型
     * @return Optional 包装的查询结果
     */
    public <T> Optional<T> queryForObjectOptional(RowMapper<T> mapper) {
        return Optional.ofNullable(queryForObject(mapper));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> RowMapper<T> typeToMapper(Class<T> requiredType) {
        return requiredType.isEnum()
                ? new SingleColumnFieldRowMapper(new EnumFieldMapper(requiredType))
                : new SingleColumnRowMapper<>(requiredType);
    }

    /**
     * 查询单个值，使用简单类型映射。支持枚举类型的自动映射。
     * <p>若无匹配记录返回 null。
     *
     * @param requiredType 期望的返回类型
     * @param <T>          结果类型
     * @return 查询结果，无数据时返回 null
     */
    public <T> T queryForValue(Class<T> requiredType) {
        return queryForObject(typeToMapper(requiredType));
    }

    /**
     * 查询单个值，以 {@link Optional} 包装返回。
     *
     * @param requiredType 期望的返回类型
     * @param <T>          结果类型
     * @return Optional 包装的查询结果
     */
    public <T> Optional<T> queryForValueOptional(Class<T> requiredType) {
        return Optional.ofNullable(queryForValue(requiredType));
    }

    /**
     * 查询单个值，使用自定义 {@link FieldMapper} 映射结果。
     *
     * @param mapper 字段映射器
     * @param <T>    结果类型
     * @return 查询结果，无数据时返回 null
     * @see FieldMapper
     */
    public <T> T queryForValue(FieldMapper<T> mapper) {
        return queryForObject(new SingleColumnFieldRowMapper<>(mapper));
    }

    /**
     * 查询单个值，使用自定义 {@link FieldMapper} 映射结果，以 {@link Optional} 包装返回。
     *
     * @param mapper 字段映射器
     * @param <T>    结果类型
     * @return Optional 包装的查询结果
     */
    public <T> Optional<T> queryForValueOptional(FieldMapper<T> mapper) {
        return Optional.ofNullable(queryForValue(mapper));
    }

    /**
     * 查询字符串值，无数据时返回空字符串。
     *
     * @return 查询结果，无数据时返回 {@code ""}
     */
    public String queryForString() {
        return queryForValueOptional(String.class).orElse(StrUtil.EMPTY);
    }

    /**
     * 查询字符串值，以 {@link Optional} 包装返回。
     *
     * @return Optional 包装的字符串结果
     */
    public Optional<String> queryForStringOptional() {
        return queryForValueOptional(String.class);
    }

    /**
     * 查询整数值，无数据时返回 0。
     *
     * @return 查询结果，无数据时返回 0
     */
    public int queryForInt() {
        return queryForValueOptional(Integer.class).orElse(0);
    }

    /**
     * 查询整数值，以 {@link Optional} 包装返回。
     *
     * @return Optional 包装的整数结果
     */
    public Optional<Integer> queryForIntOptional() {
        return queryForValueOptional(Integer.class);
    }

    /**
     * 查询双精度浮点数值，无数据时返回 0.0。
     *
     * @return 查询结果，无数据时返回 0.0
     */
    public double queryForDouble() {
        return queryForValueOptional(Double.class).orElse(0.0);
    }

    /**
     * 查询双精度浮点数值，以 {@link Optional} 包装返回。
     *
     * @return Optional 包装的双精度浮点数结果
     */
    public Optional<Double> queryForDoubleOptional() {
        return queryForValueOptional(Double.class);
    }

    /**
     * 查询日期值，无数据时返回 null。
     *
     * @return 查询结果，无数据时返回 null
     */
    public LocalDate queryForDate() {
        return queryForValue(LocalDate.class);
    }

    /**
     * 查询日期值，以 {@link Optional} 包装返回。
     *
     * @return Optional 包装的日期结果
     */
    public Optional<LocalDate> queryForDateOptional() {
        return queryForValueOptional(LocalDate.class);
    }

    /**
     * 查询单条记录并映射为指定类型的对象。
     * <p>简单类型（String、Integer 等）使用单列映射，复杂类型使用 {@link BeanMapper} 进行属性映射。
     *
     * @param objectType 期望的返回类型
     * @param <T>        结果类型
     * @return 查询结果，无数据时返回 null
     */
    public <T> T queryForObject(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValue(objectType);
        return queryForObject(BeanMapper.of(objectType));
    }

    /**
     * 查询单条记录并映射为指定类型的对象，以 {@link Optional} 包装返回。
     *
     * @param objectType 期望的返回类型
     * @param <T>        结果类型
     * @return Optional 包装的查询结果
     */
    public <T> Optional<T> queryForObjectOptional(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForValueOptional(objectType);
        return queryForObjectOptional(BeanMapper.of(objectType));
    }

    /**
     * 查询单条记录并返回为 Map，无数据时返回空 Map。
     *
     * @return 以列名为 key 的 Map，无数据时返回空 Map
     */
    public Map<String, Object> queryForMap() {
        Map<String, Object> result = queryForObject(MapRowMapper.INSTANCE);
        return result == null ? Collections.emptyMap() : result;
    }

    /**
     * 执行查询，使用 {@link RowCallbackHandler} 逐行处理结果集。
     *
     * @param rch 行回调处理器
     */
    public void query(RowCallbackHandler rch) {
        if (params.isEmpty())
            dba.getJdbcTemplate().query(getSql(), rch);
        else
            dba.getJdbcTemplate().query(getSql(), new ArgumentSetter(params), rch);
    }

    /**
     * 执行查询，使用 {@link ResultSetExtractor} 提取结果。
     *
     * @param rse 结果集提取器
     * @param <T> 结果类型
     * @return 提取的结果
     */
    public <T> T query(ResultSetExtractor<T> rse) {
        return query(getSql(), rse);
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

    /**
     * 查询列表，使用自定义 RowMapper 映射每一行。
     *
     * @param rowMapper 行映射器
     * @param <T>       结果元素类型
     * @return 查询结果列表
     */
    public <T> List<T> queryForList(RowMapper<T> rowMapper) {
        return queryForList(getSql(), rowMapper);
    }

    /**
     * 查询列表，每行结果以列名为 key 的 Map 表示。
     *
     * @return Map 列表
     */
    public List<Map<String, Object>> queryForList() {
        return queryForList(MapRowMapper.INSTANCE);
    }

    /**
     * 查询列表并映射为指定类型的对象列表。
     * <p>简单类型使用单列映射，复杂类型使用 {@link BeanMapper} 进行属性映射。
     *
     * @param objectType 期望的返回元素类型
     * @param <T>        结果元素类型
     * @return 查询结果列表
     */
    public <T> List<T> queryForList(Class<T> objectType) {
        if (BeanUtils.isSimpleValueType(objectType))
            return queryForList(typeToMapper(objectType));
        return queryForList(BeanMapper.of(objectType));
    }

    /**
     * 查询列表，使用自定义 {@link FieldMapper} 映射单列值。
     *
     * @param fieldMapper 字段映射器
     * @param <T>         结果元素类型
     * @return 查询结果列表
     * @throws DataAccessException 数据访问异常
     */
    public <T> List<T> queryForList(FieldMapper<T> fieldMapper) throws DataAccessException {
        return queryForList(new SingleColumnFieldRowMapper<>(fieldMapper));
    }

    private record OptimizeInfo(int fromIndex, int endIndex) {
    }

    private OptimizeInfo getCountOptimizeInfo() {
        if (!countOptimization) return null;
        String sql = getSql().toUpperCase();
        if (sql.contains("DISTINCT") || sql.contains(DbaUtil.GROUP_BY) || sql.contains(" UNION ")) return null;
        int fromIndex = sql.indexOf("FROM ");
        int endIndex = sql.lastIndexOf(DbaUtil.ORDER_BY);
        if (endIndex == -1) endIndex = sql.length();
        return new OptimizeInfo(fromIndex, endIndex);
    }

    /**
     * 执行 COUNT 查询，返回满足条件的记录数。
     * <p>默认启用计数优化：当 SQL 中不包含 DISTINCT、GROUP BY、UNION 时，
     * 会移除 ORDER BY 和 SELECT 列表，直接生成 {@code SELECT COUNT(*) FROM ...} 形式。
     * 可通过 {@link #disableCountOptimization()} 关闭此优化。
     *
     * @return 记录数
     */
    public int count() {
        String sql = getSql();
        OptimizeInfo optimizeInfo = getCountOptimizeInfo();
        if (optimizeInfo == null) {
            sql = String.format("SELECT COUNT(*) FROM (%s) C", sql);
        } else {
            sql = "SELECT COUNT(*) " + sql.substring(optimizeInfo.fromIndex, optimizeInfo.endIndex);
        }
        Integer result = dba.getJdbcTemplate().queryForObject(sql, Integer.class, params.toArray());
        return result == null ? 0 : result;
    }

    /**
     * 分页查询，使用自定义 RowMapper 映射结果。
     * <p>先执行 COUNT 查询获取总记录数，若总数为 0 或超出页码范围则返回空结果。
     * 第一页使用 LIMIT 优化，其余页使用数据库方言的分页 SQL。
     *
     * @param mapper   行映射器
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页记录数
     * @param <T>      结果元素类型
     * @return 分页数据对象
     * @see PageData
     */
    public <T> PageData<T> pageQuery(RowMapper<T> mapper, int pageNo, int pageSize) {
        int count = count();
        if (count == 0 || count <= (pageNo - 1) * pageSize)
            return new PageData<>(count);
        String sql = pageNo == 1 ? dba.getDialect().wrapLimitSql(getSql(), pageSize)
                : dba.getDialect().wrapPagedSql(getSql(), pageNo, pageSize);
        List<T> list = queryForList(sql, mapper);
        return new PageData<>(count, list);
    }

    /**
     * 分页查询，每行结果以列名为 key 的 Map 表示。
     *
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页记录数
     * @return 分页数据对象
     */
    public PageData<Map<String, Object>> pageQuery(int pageNo, int pageSize) {
        return pageQuery(MapRowMapper.INSTANCE, pageNo, pageSize);
    }

    /**
     * 分页查询并映射为指定类型的对象列表。
     * <p>简单类型使用单列映射，复杂类型使用 {@link BeanMapper} 进行属性映射。
     *
     * @param objectType 期望的返回元素类型
     * @param pageNo     页码（从 1 开始）
     * @param pageSize   每页记录数
     * @param <T>        结果元素类型
     * @return 分页数据对象
     */
    public <T> PageData<T> pageQuery(Class<T> objectType, int pageNo, int pageSize) {
        if (BeanUtils.isSimpleValueType(objectType)) {
            return pageQuery(typeToMapper(objectType), pageNo, pageSize);
        }
        return pageQuery(BeanMapper.of(objectType), pageNo, pageSize);
    }


    /**
     * 查询结果映射为 Map，使用自定义函数提取 key 和 value。
     *
     * @param keyFunc       从 ResultSet 提取 Map key 的函数
     * @param valueFunction 从 ResultSet 提取 Map value 的函数
     * @param <K>           key 类型
     * @param <V>           value 类型
     * @return 以 key-value 形式组织的 Map
     */
    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, ResultSetFunction<V> valueFunction) {
        Map<K, V> result = new HashMap<>();
        query((rs) -> {
            K key = keyFunc.apply(rs);
            V value = valueFunction.apply(rs);
            result.put(key, value);
        });
        return result;
    }

    /**
     * 查询结果映射为 Map，使用自定义函数提取 key，RowMapper 映射 value，并指定 Map 实例提供者。
     *
     * @param keyFunc   从 ResultSet 提取 Map key 的函数
     * @param rowMapper 行映射器，用于映射 value
     * @param supplier  Map 实例提供者（如 LinkedHashMap::new 以保持插入顺序）
     * @param <K>       key 类型
     * @param <V>       value 类型
     * @return 以 key-value 形式组织的 Map
     */
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

    /**
     * 查询结果映射为 Map，使用自定义函数提取 key，RowMapper 映射 value。
     *
     * @param keyFunc   从 ResultSet 提取 Map key 的函数
     * @param rowMapper 行映射器，用于映射 value
     * @param <K>       key 类型
     * @param <V>       value 类型
     * @return 以 key-value 形式组织的 HashMap
     */
    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, RowMapper<V> rowMapper) {
        return queryToMap(keyFunc, rowMapper, HashMap::new);
    }


    /**
     * 查询结果映射为 Map，使用自定义函数提取 key，实体类映射 value，并指定 Map 实例提供者。
     *
     * @param keyFunc  从 ResultSet 提取 Map key 的函数
     * @param clazz    value 的实体类型
     * @param supplier Map 实例提供者
     * @param <K>      key 类型
     * @param <V>      value 类型
     * @return 以 key-value 形式组织的 Map
     */
    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, Class<V> clazz, Supplier<Map<K, V>> supplier) {
        return queryToMap(keyFunc, BeanMapper.of(clazz), supplier);
    }

    /**
     * 查询结果映射为 Map，使用自定义函数提取 key，实体类映射 value。
     *
     * @param keyFunc 从 ResultSet 提取 Map key 的函数
     * @param clazz   value 的实体类型
     * @param <K>     key 类型
     * @param <V>     value 类型
     * @return 以 key-value 形式组织的 HashMap
     */
    public <K, V> Map<K, V> queryToMap(ResultSetFunction<K> keyFunc, Class<V> clazz) {
        return queryToMap(keyFunc, BeanMapper.of(clazz));
    }


    /**
     * 查询结果映射为 Map，使用自定义函数提取 key，每行以列名为 key 的 Map 作为 value。
     *
     * @param keyFunc 从 ResultSet 提取 Map key 的函数
     * @param <K>     key 类型
     * @return 以 key-Map 形式组织的 HashMap
     */
    public <K> Map<K, Map<String, Object>> queryToMap(ResultSetFunction<K> keyFunc) {
        return queryToMap(keyFunc, MapRowMapper.INSTANCE);
    }

    /**
     * 查询结果按 key 分组，使用自定义函数提取分组 key，RowMapper 映射每行值。
     * <p>相同 key 的记录会被归入同一个 List。
     *
     * @param keyFunc   从 ResultSet 提取分组 key 的函数
     * @param rowMapper 行映射器
     * @param <K>       分组 key 类型
     * @param <T>       列表元素类型
     * @return 以 key-List 形式组织的分组 Map
     */
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

    /**
     * 查询结果按 key 分组，使用自定义函数提取分组 key，实体类映射每行值。
     *
     * @param keyFunc 从 ResultSet 提取分组 key 的函数
     * @param clazz   列表元素的实体类型
     * @param <K>     分组 key 类型
     * @param <T>     列表元素类型
     * @return 以 key-List 形式组织的分组 Map
     */
    public <K, T> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc, Class<T> clazz) {
        return queryToGroup(keyFunc, BeanMapper.of(clazz));
    }

    /**
     * 查询结果按 key 分组，使用自定义函数分别提取分组 key 和列表元素值。
     *
     * @param keyFunc   从 ResultSet 提取分组 key 的函数
     * @param valueFunc 从 ResultSet 提取列表元素值的函数
     * @param <K>       分组 key 类型
     * @param <T>       列表元素类型
     * @return 以 key-List 形式组织的分组 Map
     */
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

    /**
     * 查询结果按 key 分组，每行以列名为 key 的 Map 作为列表元素。
     *
     * @param keyFunc 从 ResultSet 提取分组 key 的函数
     * @param <K>     分组 key 类型
     * @return 以 key-List(Map) 形式组织的分组 Map
     */
    public <K> Map<K, List<Map<String, Object>>> queryToGroup(ResultSetFunction<K> keyFunc) {
        return queryToGroup(keyFunc, MapRowMapper.INSTANCE);
    }


    /**
     * 查询树形结构数据，结果集需包含 ID 和 PID 列。
     * <p>使用实体类进行行映射，实体类需实现 {@link ITreeNode} 接口。
     *
     * @param objectType 树节点实体类型
     * @param <T>        树节点类型
     * @return 树形结构对象
     * @see Tree
     * @see ITreeNode
     */
    public <T extends ITreeNode<T>> Tree<T> queryForTree(Class<T> objectType) {
        return queryForTree(BeanMapper.of(objectType));
    }

    /**
     * 查询树形结构数据，结果集需包含 ID 和 PID 列。
     * <p>使用自定义 RowMapper 进行行映射，根据 ID/PID 关系自动构建父子层级。
     * PID 在当前结果集中找不到对应父节点的记录将作为根节点。
     *
     * @param mapper 行映射器
     * @param <T>    树节点类型，需实现 {@link ITreeNode}
     * @return 树形结构对象
     * @see Tree
     * @see ITreeNode
     */
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

    /**
     * 禁用 COUNT 查询优化。
     * <p>默认情况下，{@link #count()} 和 {@link #exist()} 会尝试优化 SQL（移除 ORDER BY、
     * 替换 SELECT 列表为 COUNT(*)）。当 SQL 包含 DISTINCT、GROUP BY、UNION 时自动跳过优化。
     * 调用此方法可强制关闭优化，始终使用子查询方式计数。
     *
     * @return this
     */
    public Sql disableCountOptimization() {
        this.countOptimization = false;
        return this;
    }

    /**
     * 判断当前查询是否存在匹配的记录。
     * <p>通过优化后的 LIMIT 1 查询判断，比 COUNT 更高效。
     *
     * @return 存在返回 true，否则返回 false
     */
    public boolean exist() {
        String sql = getSql();
        OptimizeInfo optimizeInfo = getCountOptimizeInfo();
        if (optimizeInfo == null) {
            sql = dba.getDialect().wrapLimitSql(sql, 1);
            sql = String.format("SELECT 1 FROM (%s) C", sql);
        } else {
            sql = "select 1 " + sql.substring(optimizeInfo.fromIndex, optimizeInfo.endIndex);
            sql = dba.getDialect().wrapLimitSql(sql, 1);
        }
        try {
            dba.getJdbcTemplate().queryForObject(sql, Integer.class, params.toArray());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * 设置查询返回的最大记录数，等价于 {@code range(null, limit)}。
     * <p>最终 SQL 会通过数据库方言包装 LIMIT 子句。
     *
     * @param limit 最大记录数
     * @return this
     */
    public Sql limit(int limit) {
        return range(null, limit);
    }

    /**
     * 设置查询的范围（偏移量和限制数）。
     * <p>from 为 null 时仅限制返回数量，等价于 LIMIT；from 不为 null 时使用 OFFSET/LIMIT 分页。
     * 最终 SQL 会通过数据库方言包装相应的分页子句。
     *
     * @param from 偏移量，null 表示不设置偏移
     * @param to   返回的最大记录数
     * @return this
     * @see Range
     */
    public Sql range(Integer from, int to) {
        this.range = Range.of(from, to);
        return this;
    }

    /**
     * 根据指定的字段创建查询用的 Sql 对象。
     * <p>不传字段时生成 {@code SELECT *}；
     * 传入多个字段时自动用逗号连接。
     *
     * @param fields 需要查询的字段名，不传默认为 *
     * @return 新的 Sql 对象
     */
    public static Sql select(String... fields) {
        return switch (fields.length) {
            case 0 -> new Sql(SELECT).append("*");
            case 1 -> new Sql(SELECT).append(fields[0]);
            default -> new Sql(SELECT).join(fields);
        };
    }

}
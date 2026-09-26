package io.github.jinghui70.rainbow.dbaccess.crud;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.*;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.sql.OrderBy;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import io.github.jinghui70.rainbow.dbaccess.utils.StringBuilderX;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查询 DTO，封装 SELECT 字段、WHERE 条件、排序和分页参数。
 * <p>
 * 通过 {@link Cnd} 链式添加查询条件，通过 {@link OrderBy} 指定排序。排序按以下规则组装：
 * <ol>
 *     <li>客户端传入了 {@code orderBys} 就用它，否则用 {@code defaultOrderBys}（由 {@link #defaultOrderBy(String...)} 设置）</li>
 *     <li>无论上一步结果如何，都把实体主键列追加到末尾——主键在 {@link #setEntity(Class)} 时按
 *     {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 注解取得</li>
 *     <li>按列名去重（大小写不敏感），已经排过的列不会重复追加</li>
 * </ol>
 * 主键始终兜底是为了让排序全序确定：非唯一列排序时各数据库对并列行的返回顺序没有保证，
 * 分页会出现某行重复出现或整页丢失。
 * <p>
 * 前端可通过 JSON 反序列化直接构造此对象。
 */
public class QueryDTO {

    private String entity;

    private String fields;

    private List<Cnd> cnds;

    private List<OrderBy> orderBys;

    private int pageNo;

    private int pageSize;

    /** 客户端未传 {@code orderBys} 时使用的默认排序 */
    protected List<OrderBy> defaultOrderBys;

    /** 实体主键列，始终追加到排序末尾，保证分页顺序稳定 */
    protected List<OrderBy> keyOrderBys;

    /**
     * 获取目标表名。
     *
     * @return 表名
     */
    public String getEntity() {
        return entity;
    }

    /**
     * 设置目标表名。
     * <p>
     * 表名是裸字符串时无从得知主键，会清掉之前记录的主键排序兜底——
     * 把表换成 JOIN 之后，原实体的裸主键列在 JOIN 中可能有歧义。
     * 这类查询要保证分页稳定，请用 {@link #defaultOrderBy(String...)} 显式指定带前缀的唯一列。
     *
     * @param entity 表名，也可以是 JOIN 片段
     * @return this
     */
    public QueryDTO setEntity(String entity) {
        this.entity = entity;
        this.keyOrderBys = null;
        return this;
    }

    /**
     * 通过实体类设置表名，并记录主键列作为排序兜底。
     *
     * @param entityClass 实体类，需含 {@code @Table} 注解
     * @return this
     */
    public QueryDTO setEntity(Class<?> entityClass) {
        this.setEntity(DbaUtil.tableName(entityClass));
        this.keyOrderBys = PropInfoCache.get(entityClass).values().stream()
                .filter(prop -> prop.getId() != null)
                .map(prop -> new OrderBy(prop.getFieldName(), false))
                .toList();
        return this;
    }

    public String getFields() {
        return (fields == null) ? "*" : fields;
    }

    public QueryDTO setFields(String... fields) {
        this.fields = switch (fields.length) {
            case 0 -> null;
            case 1 -> fields[0];
            default -> new StringBuilderX().join(fields).toString();
        };
        return this;
    }

    /**
     * 设置查询条件列表。
     *
     * @param cnds 条件列表
     */
    public void setCnds(List<Cnd> cnds) {
        if (CollUtil.isNotEmpty(cnds)) checkCnds(cnds);
        this.cnds = cnds;
    }

    private void checkCnds(List<Cnd> cnds) {
        for (Cnd cnd : cnds) {
            if (CollUtil.isNotEmpty(cnd.getChildren())) checkCnds(cnd.getChildren());
            else DbaUtil.validateFieldName(cnd.getField());
        }
    }

    /**
     * 获取查询条件列表。
     *
     * @return 条件列表
     */
    public List<Cnd> getCnds() {
        return cnds;
    }

    /**
     * 设置排序条件列表，字段名会按 SQL 标识符规则校验。
     *
     * @param orderBys 排序条件
     * @throws IllegalArgumentException 字段名不是合法标识符时抛出
     */
    public void setOrderBys(List<OrderBy> orderBys) {
        if (CollUtil.isNotEmpty(orderBys))
            for (OrderBy orderBy : orderBys) {
                DbaUtil.validateFieldName(orderBy.getField());
            }
        this.orderBys = orderBys;
    }

    /**
     * 获取排序条件列表。
     *
     * @return 排序条件
     */
    public List<OrderBy> getOrderBys() {
        return orderBys;
    }

    /**
     * 获取页码。
     *
     * @return 页码
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 设置页码。
     *
     * @param pageNo 页码
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 添加一个查询条件。
     *
     * @param cnd 条件，为 {@code null} 时忽略
     * @return this
     */
    public QueryDTO addCnd(Cnd cnd) {
        if (cnd == null) return this;
        if (cnds == null) cnds = new LinkedList<>();
        cnds.add(cnd);
        return this;
    }

    /**
     * 设置客户端未传 {@code orderBys} 时使用的默认排序。
     * <p>
     * 不影响主键兜底——主键仍会追加到排序末尾。
     *
     * @param keys 列名，默认升序，需要降序传 {@code "NAME DESC"}
     * @return this
     */
    public QueryDTO defaultOrderBy(String... keys) {
        this.defaultOrderBys = Arrays.stream(keys).map(OrderBy::parse).toList();
        return this;
    }

    /**
     * 设置客户端未传 {@code orderBys} 时使用的默认排序。
     *
     * @param orderBys 排序条件
     * @return this
     */
    public QueryDTO defaultOrderBy(OrderBy... orderBys) {
        this.defaultOrderBys = List.of(orderBys);
        return this;
    }

    /**
     * 构建 {@link Sql} 对象，包含 SELECT 字段、FROM 表、WHERE 条件和排序。
     *
     * @param dba 数据库访问对象
     * @return Sql 对象
     */
    public Sql getSql(Dba dba) {
        Sql sql = dba.select(getFields());
        sql.from(getEntity());
        processCnd(sql);
        processOrderBy(sql);
        return sql;
    }

    /**
     * 将查询条件拼接到 SQL 中。
     *
     * @param sql Sql 对象
     */
    protected void processCnd(Sql sql) {
        if (CollUtil.isNotEmpty(cnds))
            for (Cnd cnd : cnds) {
                sql.where(cnd);
            }
    }

    /**
     * 将排序条件拼接到 SQL 中。
     * <p>
     * 优先使用客户端传入的排序，没有则用默认排序；两种情况都会把主键列追加到末尾做兜底，
     * 已经排过的列不重复追加。
     *
     * @param sql Sql 对象
     */
    protected void processOrderBy(Sql sql) {
        List<OrderBy> result = new ArrayList<>();
        Set<String> used = new HashSet<>();
        addOrderBys(result, used, CollUtil.isNotEmpty(orderBys) ? orderBys : defaultOrderBys);
        addOrderBys(result, used, keyOrderBys);
        sql.orderBy(result);
    }

    /**
     * 把排序项逐个追加到结果中，列名已经出现过的跳过。
     *
     * @param result 排序项结果列表
     * @param used   已出现过的列名（大写）
     * @param items  待追加的排序项，为空时忽略
     */
    private void addOrderBys(List<OrderBy> result, Set<String> used, List<OrderBy> items) {
        if (CollUtil.isEmpty(items)) return;
        for (OrderBy item : items) {
            if (used.add(item.getField().toUpperCase())) result.add(item);
        }
    }

    /**
     * 执行分页查询，以实体类映射结果。
     *
     * @param dba        数据库访问对象
     * @param objectType 实体类
     * @param <T>        实体类型
     * @return 分页数据
     */
    public <T> PageData<T> queryPage(Dba dba, Class<T> objectType) {
        return getSql(dba).queryPage(objectType, pageNo, pageSize);
    }

    /**
     * 执行分页查询，以自定义 {@link RowMapper} 映射结果。
     *
     * @param dba    数据库访问对象
     * @param mapper 行映射器
     * @param <T>    结果类型
     * @return 分页数据
     */
    public <T> PageData<T> queryPage(Dba dba, RowMapper<T> mapper) {
        return getSql(dba).queryPage(mapper, pageNo, pageSize);
    }

    /**
     * 执行分页查询，以 Map 返回结果。
     *
     * @param dba 数据库访问对象
     * @return 分页数据
     */
    public PageData<Map<String, Object>> queryPage(Dba dba) {
        return getSql(dba).queryPage(pageNo, pageSize);
    }

    /**
     * 执行列表查询，以实体类映射结果。
     *
     * @param dba        数据库访问对象
     * @param objectType 实体类
     * @param <T>        实体类型
     * @return 结果列表
     */
    public <T> List<T> query(Dba dba, Class<T> objectType) {
        return getSql(dba).queryForList(objectType);
    }

    /**
     * 执行列表查询，以自定义 {@link RowMapper} 映射结果。
     *
     * @param dba    数据库访问对象
     * @param mapper 行映射器
     * @param <T>    结果类型
     * @return 结果列表
     */
    public <T> List<T> query(Dba dba, RowMapper<T> mapper) {
        return getSql(dba).queryForList(mapper);
    }

    /**
     * 执行列表查询，以 Map 返回结果。
     *
     * @param dba 数据库访问对象
     * @return 结果列表
     */
    public List<Map<String, Object>> query(Dba dba) {
        return getSql(dba).queryForList();
    }
}

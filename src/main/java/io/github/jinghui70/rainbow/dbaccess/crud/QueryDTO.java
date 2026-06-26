package io.github.jinghui70.rainbow.dbaccess.crud;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.*;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.sql.OrderBy;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import io.github.jinghui70.rainbow.dbaccess.utils.StringBuilderX;
import org.springframework.jdbc.core.RowMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.SELECT;

/**
 * 查询 DTO，封装 SELECT 字段、WHERE 条件、排序和分页参数。
 * <p>
 * 通过 {@link Cnd} 链式添加查询条件，通过 {@link OrderBy} 指定排序。
 * 当未指定排序时自动使用 {@code defaultOrderBys}，该值在 {@link #setEntity(Class)} 时按 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 注解填充。
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

    protected String defaultOrderBys;

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
     *
     * @param entity 表名
     * @return this
     */
    public QueryDTO setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    /**
     * 通过实体类设置表名，并自动填充默认排序。
     *
     * @param entityClass 实体类，需含 {@code @Table} 注解
     * @return this
     */
    public QueryDTO setEntity(Class<?> entityClass) {
        this.setEntity(DbaUtil.tableName(entityClass));
        if (StrUtil.isEmpty(defaultOrderBys)) {
            this.defaultOrderBys = DbaUtil.defaultOrderBy(entityClass);
        }
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
     * 设置排序条件列表。
     *
     * @param orderBys 排序条件
     */
    public void setOrderBys(List<OrderBy> orderBys) {
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
     * 设置默认排序字段。
     *
     * @param keys 属性名，默认升序
     * @return this
     */
    public QueryDTO defaultOrderBy(String... keys) {
        this.defaultOrderBys = StrUtil.join(StrUtil.COMMA, (Object[]) keys);
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
     * 将排序条件拼接到 SQL 中，优先使用客户端传入的排序。
     *
     * @param sql Sql 对象
     */
    protected void processOrderBy(Sql sql) {
        if (CollUtil.isNotEmpty(orderBys)) sql.orderBy(orderBys);
        else if (defaultOrderBys != null) sql.orderBy(defaultOrderBys);
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

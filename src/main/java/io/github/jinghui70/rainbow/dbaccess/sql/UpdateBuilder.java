package io.github.jinghui70.rainbow.dbaccess.sql;

import cn.hutool.core.lang.Assert;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;

import java.util.*;

/**
 * 更新操作构建器，根据实体 Bean 的主键执行更新。
 * <p>
 * 以 {@code @Id} 注解的主键字段自动生成 WHERE 条件，更新所有非主键、非自增字段。
 * 默认全字段更新；通过 {@link #include} / {@link #exclude} / {@link #excludeNull} 控制参与更新的字段。
 * <p>
 * 典型用法：
 * <pre>
 *     dba.updateOf(bean).execute();                          // 全量更新
 *     dba.updateOf(bean).include("name", "age").execute();   // 仅更新 name、age
 *     dba.updateOf(bean).exclude("avatar").execute();        // 排除 avatar，更新其它字段
 *     dba.updateOf(bean).excludeNull().execute();            // null 字段不更新
 * </pre>
 */
public class UpdateBuilder {

    private final Dba dba;
    private final Object bean;

    private enum FieldFilter {ALL, INCLUDE, EXCLUDE}

    private FieldFilter fieldFilter = FieldFilter.ALL;

    // 过滤字段，Bean 的属性值
    private Set<String> fieldNames;

    // 过滤为空字段
    private boolean excludeNull;

    /**
     * @param dba  数据库访问对象
     * @param bean 实体对象，需用 {@code @Id} 标注主键
     */
    public UpdateBuilder(Dba dba, Object bean) {
        this.dba = dba;
        this.bean = bean;
    }

    private void checkFilter(FieldFilter filter) {
        boolean isOk = fieldFilter == FieldFilter.ALL || fieldFilter == filter;
        Assert.isTrue(isOk, "不能同时使用 include 和 exclude");
        this.fieldFilter = filter;
    }

    /**
     * 仅更新指定字段。和 {@link #exclude} 互斥。
     *
     * @param fields Java 属性名
     * @return this
     */
    public UpdateBuilder include(String... fields) {
        checkFilter(FieldFilter.INCLUDE);
        this.fieldNames = Set.of(fields);
        return this;
    }

    /**
     * 仅更新指定字段。和 {@link #exclude} 互斥。
     *
     * @param fields Java 属性名集合
     * @return this
     */
    public UpdateBuilder include(Collection<String> fields) {
        checkFilter(FieldFilter.INCLUDE);
        this.fieldNames = new HashSet<>(fields);
        return this;
    }

    /**
     * 排除指定字段不参与更新。和 {@link #include} 互斥。
     *
     * @param fields Java 属性名
     * @return this
     */
    public UpdateBuilder exclude(String... fields) {
        checkFilter(FieldFilter.EXCLUDE);
        this.fieldNames = Set.of(fields);
        return this;
    }

    /**
     * 排除指定字段不参与更新。和 {@link #include} 互斥。
     *
     * @param fields Java 属性名集合
     * @return this
     */
    public UpdateBuilder exclude(Collection<String> fields) {
        checkFilter(FieldFilter.EXCLUDE);
        this.fieldNames = new HashSet<>(fields);
        return this;
    }

    /**
     * null 字段不参与 SET。可以和 {@link #include}/{@link #exclude} 叠加。
     *
     * @return this
     */
    public UpdateBuilder excludeNull() {
        this.excludeNull = true;
        return this;
    }

    // 构建 UPDATE SQL 并返回 Sql 实例，供后续链式调用。
    private Sql buildSql() {
        Class<?> clazz = bean.getClass();
        Sql sql = dba.sql("UPDATE ").append(DbaUtil.tableName(clazz)).append(" SET ");
        LinkedHashMap<String, PropInfo> propMap = PropInfoCache.get(clazz);
        List<PropInfo> keyArray = propMap.values().stream().filter(p -> p.getId() != null).toList();
        for (PropInfo p : propMap.values()) {
            if (p.getId() != null || p.isAutoIncrement()) continue;
            Object value = p.getUpdateValue(dba, bean);
            if (excludeNull && value == null) continue;
            if (fieldFilter == FieldFilter.INCLUDE && !fieldNames.contains(p.getName())) continue;
            if (fieldFilter == FieldFilter.EXCLUDE && fieldNames.contains(p.getName())) continue;
            sql.append(p.getFieldName()).append("=?").addParam(value).appendTempComma();
        }
        sql.clearTemp();
        for (PropInfo p : keyArray) {
            sql.where(p.getFieldName(), p.getValue(bean));
        }
        return sql;
    }

    /**
     * 执行更新。以 {@code @Id} 主键字段自动生成 WHERE 条件。
     *
     * @return 受影响行数
     */
    public int execute() {
        return buildSql().execute();
    }

}

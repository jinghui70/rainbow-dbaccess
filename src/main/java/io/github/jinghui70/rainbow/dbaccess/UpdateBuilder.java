package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.lang.Assert;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 更新Bean操作构建器
 * 典型用法：
 * <pre>
 *     // Bean 模式
 *     dba.update(User.class).setBean(user).exclude("createTime").execute();
 *     dba.update(User.class).setBean(user).include("name", "age").excludeNull().execute();
 * </pre>
 */
public class UpdateBuilder {

    private final Dba dba;
    private final Object bean;

    // 字段过滤方式（仅 Map / Bean 模式）
    private enum FieldFilter {ALL, INCLUDE, EXCLUDE}
    private FieldFilter fieldFilter = FieldFilter.ALL;

    // 过滤字段，Bean 的属性值
    private Set<String> fieldNames;

    // 过滤为空字段
    private boolean excludeNull;

    UpdateBuilder(Dba dba, Object bean) {
        this.dba = dba;
        this.bean = bean;
    }

    /**
     * 仅更新指定字段。和 {@link #exclude} 互斥。
     * <p>Bean 模式：参数为 Java 属性名。
     * <p>Map 模式：参数为 Map key（列名）。
     */
    public UpdateBuilder include(String... fields) {
        Assert.notEquals(fieldFilter, FieldFilter.EXCLUDE, "不能同时使用 include 和 exclude");
        this.fieldFilter = FieldFilter.INCLUDE;
        this.fieldNames = Set.of(fields);
        return this;
    }

    /**
     * 排除指定字段，不参与更新。和 {@link #include} 互斥。
     * <p>Bean 模式：参数为 Java 属性名。
     * <p>Map 模式：参数为 Map key（列名）。
     */
    public UpdateBuilder exclude(String... fields) {
        Assert.notEquals(fieldFilter, FieldFilter.INCLUDE, "不能同时使用 include 和 exclude");
        this.fieldFilter = FieldFilter.EXCLUDE;
        this.fieldNames = Set.of(fields);
        return this;
    }

    /**
     * null 字段不参与 SET。可以和 {@link #include}/{@link #exclude} 叠加。
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
            Object value = p.getValue(bean);
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
     * 执行更新。
     * <p>Bean 模式：PK 自动生成 WHERE。
     * <p>SQL/SQL+Map 模式：无 WHERE 将更新全表。
     *
     * @return 受影响行数
     */
    public int execute() {
        return buildSql().execute();
    }

}

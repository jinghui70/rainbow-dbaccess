package io.github.jinghui70.rainbow.dbaccess.sql;

import io.github.jinghui70.rainbow.dbaccess.DbaUtil;

import java.util.List;

/**
 * 排序信息类，封装排序字段和排序方式（升序/降序）。
 * <p>
 * 用于构建 SQL 的 ORDER BY 子句，支持通过 {@link Sql#orderBy(List)}} 批量设置排序。
 *
 * @author lijinghui
 * @see Sql#orderBy(List)
 */
public class OrderBy {

    private String field;
    private boolean desc;
    /**
     * 默认构造函数。
     */
    public OrderBy() {
    }

    /**
     * 构造函数，指定排序字段和是否降序。
     *
     * @param field 排序字段名
     * @param desc  是否降序排序，true 为降序，false 为升序
     */
    public OrderBy(String field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    /**
     * 解析排序串，是 {@link #toString()} 的逆运算。
     * <p>
     * 字段名后可跟 {@code ASC} / {@code DESC}（大小写不敏感），不写或写别的都按升序处理。
     *
     * @param text 排序串，如 {@code "NAME"} 或 {@code "NAME DESC"}
     * @return 对应的 OrderBy 对象
     */
    public static OrderBy parse(String text) {
        String value = text.trim();
        int index = value.indexOf(' ');
        if (index < 0) return new OrderBy(value, false);
        return new OrderBy(value.substring(0, index),
                "DESC".equalsIgnoreCase(value.substring(index + 1).trim()));
    }

    /**
     * 获取排序字段名。
     *
     * @return 排序字段名
     */
    public String getField() {
        return field;
    }

    /**
     * 设置排序字段名。
     *
     * @param field 排序字段名
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * 获取是否降序排序。
     *
     * @return true 表示降序，false 表示升序
     */
    public boolean isDesc() {
        return desc;
    }

    /**
     * 设置是否降序排序。
     *
     * @param desc true 表示降序，false 表示升序
     */
    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    /**
     * 返回排序的字符串表示形式，如 "FIELD DESC" 或 "FIELD"。
     *
     * @return 排序字符串
     */
    @Override
    public String toString() {
        return desc ? DbaUtil.desc(field) : field;
    }

    /**
     * 构造降序排序，等价于 {@code new OrderBy(field, true)}。
     *
     * @param field 排序字段名
     * @return 降序的 OrderBy 对象
     */
    public static OrderBy desc(String field) {
        return new OrderBy(field, true);
    }

    /**
     * 构造升序排序，等价于 {@code new OrderBy(field, false)}。
     *
     * @param field 排序字段名
     * @return 升序的 OrderBy 对象
     */
    public static OrderBy of(String field) {
        return new OrderBy(field, false);
    }
}

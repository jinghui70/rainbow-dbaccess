package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;

import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * UPDATE SQL 语句构建器，继承 {@link Sql} 并提供 set 相关方法。
 * <p>
 * 提供多种 set 方法用于构建 UPDATE 语句的 SET 子句，支持条件式 set、字段映射器等功能。
 * 通过 {@link Dba#update(String)} 或 {@link Dba#update(Class)} 创建实例。
 *
 * @author lijinghui
 * @see Dba#update(String)
 * @see Dba#update(Class)
 */
public class UpdateSql extends Sql {

    private boolean set = false;

    /**
     * 构造函数，内部使用。
     *
     * @param dba   Dba 对象
     * @param table 表名
     */
    UpdateSql(Dba dba, String table) {
        super(dba);
        append("UPDATE ").append(table).append(" SET ");
    }

    /**
     * 内部方法，处理 SET 子句的逗号分隔。
     * <p>
     * 首次调用时不添加逗号，后续调用在前面添加逗号。
     */
    private void set() {
        if (set)
            append(StrUtil.COMMA);
        else
            set = true;
    }

    /**
     * 拼update语句的一个set部分
     *
     * @param field 字段
     * @param value 新值
     * @return 返回自己
     */
    public UpdateSql set(String field, Object value) {
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
    public UpdateSql set(String field, FieldMapper<?> fieldMapper, Object value) {
        set();
        append(field).append("=?").addParam(new FieldValue(value, fieldMapper));
        return this;
    }

    /**
     * 通过 Map，设置一组数据
     *
     * @param map Map数据
     * @return this
     */
    public UpdateSql setMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 拼update 语句的一个set部分，主要是用于字段间计算
     *
     * @param setStr 更新字符串，如 COUNT=COUNT+1
     * @return this
     */
    public UpdateSql set(String setStr) {
        set();
        append(setStr);
        return this;
    }

    /**
     * 条件式 set 方法，仅当 condition 为 true 时执行 set(field, value)。
     *
     * @param condition 是否执行 set 的条件
     * @param field     字段名
     * @param value     新值
     * @return this
     */
    public UpdateSql set(boolean condition, String field, Object value) {
        return condition ? set(field, value) : this;
    }

    /**
     * 条件式 set 方法，仅当 condition 为 true 时执行 set(setStr)。
     *
     * @param condition 是否执行 set 的条件
     * @param setStr    更新字符串
     * @return this
     */
    public UpdateSql set(boolean condition, String setStr) {
        return condition ? set(setStr) : this;
    }

    /**
     * 条件式 set 方法，仅当 condition 为 true 时执行 set(field, fieldMapper, value)。
     *
     * @param condition   是否执行 set 的条件
     * @param field       字段名
     * @param fieldMapper 字段映射器
     * @param value       新值
     * @return this
     */
    public UpdateSql set(boolean condition, String field, FieldMapper<?> fieldMapper, Object value) {
        return condition ? set(field, fieldMapper, value) : this;
    }

}

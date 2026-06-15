package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;

import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

public class UpdateSql extends Sql {

    private boolean set = false;

    UpdateSql(Dba dba, String table) {
        super(dba);
        append("UPDATE ").append(table).append(" SET ");
    }

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
     * @return this;
     */
    public UpdateSql set(String setStr) {
        set();
        append(setStr);
        return this;
    }

    public UpdateSql set(boolean condition, String field, Object value) {
        return condition ? set(field, value) : this;
    }

    public UpdateSql set(boolean condition, String set) {
        return condition ? set(set) : this;
    }

    public UpdateSql set(boolean condition, String field, FieldMapper<?> fieldMapper, Object value) {
        return condition ? set(field, fieldMapper, value) : this;
    }

}

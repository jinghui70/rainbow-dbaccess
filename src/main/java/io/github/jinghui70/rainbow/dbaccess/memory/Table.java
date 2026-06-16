package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.utils.StringBuilderX;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 内存数据库表定义
 * <p>
 * 用于描述内存数据库表的结构，包括表名和字段列表，可生成建表DDL语句
 * </p>
 */
public class Table {

    /**
     * 默认表名
     */
    public static final String DEFAULT_NAME = "X";
    private final List<Field> fields;
    private String name;

    /**
     * 构造函数
     *
     * @param name   表名
     * @param fields 字段列表
     */
    public Table(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    /**
     * 构造函数
     *
     * @param name   表名
     * @param fields 字段数组
     */
    public Table(String name, Field... fields) {
        this.name = name;
        if (fields.length == 0)
            this.fields = new ArrayList<>();
        else
            this.fields = CollUtil.newArrayList(fields);
    }

    /**
     * 构造函数，使用默认表名"X"
     *
     * @param fields 字段数组
     */
    public Table(Field... fields) {
        this(DEFAULT_NAME, fields);
    }

    /**
     * 判断表是否有主键字段
     *
     * @return true表示有主键字段
     */
    public boolean hasKey() {
        if (CollUtil.isEmpty(fields))
            return false;
        return fields.parallelStream().anyMatch(Field::isKey);
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置表名
     *
     * @param name 表名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * 生成建表DDL语句
     *
     * @return 建表SQL语句
     */
    public String ddl() {
        StringBuilderX sql = new StringBuilderX("create table ").append(name).append("(");
        for (Field field : fields) {
            sql.append(field.getName()).append(StrUtil.SPACE).append(field.getType().name());
            switch (field.getType()) {
                case CHAR:
                case VARCHAR:
                    sql.append("(").append(field.getLength()).append(")");
                    break;
                case NUMERIC:
                    sql.append("(").append(field.getLength()).append(StrUtil.COMMA).append(field.getPrecision())
                            .append(")");
                    break;
                default:
                    break;
            }
            // 自增主键
            if (field.isKey() && field.isAutoIncrement())
                sql.append(" AUTO_INCREMENT");
            if (Objects.nonNull(field.getDefaultValue())) {
                sql.append(" DEFAULT ").append(field.getDefaultValue());
            }
            sql.appendTempComma();
        }
        if (hasKey()) {
            sql.append("PRIMARY KEY(");
            for (Field field : fields) {
                if (field.isKey()) {
                    sql.append(field.getName()).appendTempComma();
                }
            }
            sql.clearTemp().append(")");
        }
        sql.clearTemp().append(")");
        return sql.toString();
    }
}

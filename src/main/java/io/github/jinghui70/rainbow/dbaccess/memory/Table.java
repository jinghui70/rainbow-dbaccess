package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.utils.StringBuilderX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Table {

    public static final String DEFAULT = "X";

    private String name;

    private final List<Field> fields;

    public Table(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    public static Table create(Field... fields) {
        Table table = create(DEFAULT);
        return table.add(fields);
    }

    public static Table create(List<Field> fields) {
        return new Table(DEFAULT, fields);
    }

    public static Table create(String name) {
        return new Table(name, new ArrayList<>());
    }

    public boolean hasKey() {
        if (CollUtil.isEmpty(fields))
            return false;
        return fields.parallelStream().anyMatch(Field::isKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Table add(Field... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

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


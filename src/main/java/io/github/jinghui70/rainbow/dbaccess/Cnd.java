package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

public class Cnd {

    public static final String IN = " in ";
    public static final String NOT_IN = " not in ";
    public static final String LIKE = " like ";
    public static final String NOT_LIKE = " not like ";

    public static final List<String> OPERATORS = Arrays.asList("<=", "<", ">=", ">", "=", "!=", IN, LIKE, NOT_IN,
            NOT_LIKE);

    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";

    private String field;

    private String op = "";

    private Object value;

    public Cnd() {
    }

    public Cnd(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public Cnd(String field, String op, Object value) {
        this.field = field;
        setOp(op);
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        op = op.toLowerCase();
        Assert.isTrue(Cnd.OPERATORS.contains(op), "bad condition op: {}", op);
        this.op = op;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void toSql(Sql sql) {
        sql.append(field);
        if (value instanceof Sql) {
            if (StrUtil.isEmpty(op))
                sql.append(Cnd.IN);
            else
                sql.append(op);
            sql.append("(").append((Sql) value).append(")");
            return;
        }

        switch (op) {
            case "":
            case "=":
                if (value == null)
                    sql.append(" is null");
                else if (ArrayUtil.isArray(value) || value instanceof Collection) {
                    inSql(sql);
                } else if (!rangeSql(sql))
                    sql.append("=?").addParam(enumCheck(value));
                break;
            case "!=":
                if (value == null)
                    sql.append(" is not null");
                else
                    sql.append("!=?").addParam(enumCheck(value));
                break;
            case LIKE:
            case NOT_LIKE:
                sql.append(op).append("?").addParam('%' + value.toString() + '%');
                break;
            case IN:
                inSql(sql);
                break;
            case NOT_IN:
                notInSql(sql);
                break;
            default:
                sql.append(op).append("?").addParam(enumCheck(value));
                break;
        }
    }

    private boolean rangeSql(Sql sql) {
        Range<?> range = paramToRange();
        if (range == null)
            return false;
        range.regular();
        if (range.singleValue())
            sql.append("=?").addParam(enumCheck(range.getFrom()));
        else if (range.getFrom() != null) {
            if (range.getTo() == null) {
                sql.append(">=?").addParam(enumCheck(range.getFrom()));
            } else {
                sql.append(StrUtil.SPACE).append("between ? and ?").addParam(enumCheck(range.getFrom()), enumCheck(range.getTo()));
            }
        } else {
            sql.append("<=?").addParam(enumCheck(range.getTo()));
        }
        return true;
    }

    public void toNamedSql(NamedSql sql) {
        switch (op) {
            case "=":
                Assert.notNull(value, "condition value should not be null");
                if (!rangeNamedSql(sql)) {
                    sql.append(field).append(op).append(":").append(field).set(field, enumCheck(value));
                }
                break;
            case IN:
            case NOT_IN:
                throw new RuntimeException("Named Sql does not support in operator");
            default:
                sql.append(field).append(op).append(":").append(field).set(field, enumCheck(value));
                break;
        }
    }

    private Range<?> paramToRange() {
        if (value instanceof Map) {
            return BeanUtil.toBeanIgnoreCase(value, Range.class, false);
        } else if (value instanceof Range) {
            return (Range<?>) value;
        } else
            return null;
    }

    private boolean rangeNamedSql(NamedSql sql) {
        Range<?> range = paramToRange();
        if (range == null)
            return false;
        range.regular();
        sql.append(field).append(" between :").append(field).append(" and :").append(field).append("_T")
                .set(field, enumCheck(range.getFrom())).set(field + "_T", enumCheck(range.getTo()));
        return true;
    }

    private void inSql(Sql sql) {
        Object[] arr = valueToArray();
        if (arr.length == 1) {
            sql.append("=?").addParam(enumCheck(arr[0]));
        } else
            sql.append(Cnd.IN).append("(").repeat("?", arr.length).append(")")
                    .addParam(enumCheck(arr));
    }

    private void notInSql(Sql sql) {
        Object[] arr = valueToArray();
        if (arr.length == 1) {
            sql.append("!=?").addParam(enumCheck(arr[0]));
        } else
            sql.append(Cnd.NOT_IN).append("(").repeat("?", arr.length).append(")")
                    .addParam(enumCheck(arr));
    }

    public Object[] valueToArray() {
        Assert.notNull(value);
        if (value instanceof Collection) {
            return ((Collection<?>) value).toArray();
        } else if (ArrayUtil.isArray(value))
            return (Object[]) value;
        throw new RuntimeException("condition value is not a list or array");
    }

    @Override
    public String toString() {
        return "{" + field + " " + op + " " + value + "}";
    }

}

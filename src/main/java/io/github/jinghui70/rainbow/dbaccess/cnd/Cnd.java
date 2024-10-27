package io.github.jinghui70.rainbow.dbaccess.cnd;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 描述一个查询条件的对象，条件的三要素：字段名、比较符、条件值
 */
public class Cnd {

    protected String field;

    private Op op;

    private Object value;

    protected Cnd() {
    }

    @Deprecated
    public Cnd(String field, String opStr, Object value) {
        this.field = field;
        this.op = Enum.valueOf(Op.class, opStr.toUpperCase());
        this.value = value;
    }

    public Cnd(String field, Op op, Object value) {
        this.field = field;
        this.op = op;
        this.value = value;
        if (value instanceof SqlWrapper) return;
        switch (op) {
            case LIKE:
            case NOT_LIKE:
                String str = value.toString();
                if (str.startsWith("%") || str.endsWith("%")) {
                    this.value = str;
                } else
                    this.value = StrUtil.format("%{}%", str);
                break;
            case LIKE_LEFT:
            case NOT_LIKE_LEFT:
                this.value = value + "%";
                break;
            case LIKE_RIGHT:
            case NOT_LIKE_RIGHT:
                this.value = "%" + value;
                break;
            case EQ:
                if (ArrayUtil.isArray(value) || value instanceof Collection) {
                    this.op = Op.IN;
                    this.value = inValue(value);
                }
                break;
            case NE:
                if (ArrayUtil.isArray(value) || value instanceof Collection) {
                    this.op = Op.NOT_IN;
                    this.value = inValue(value);
                }
                break;
            case IN:
            case NOT_IN:
                this.value = inValue(value);
                break;
            default:
                break;
        }
    }

    private Object[] inValue(Object value) {
        Assert.notNull(value, "value of in/not_in condition cannot be null");
        Object[] array = ArrayUtil.isArray(value)
                ? (Object[]) value
                : (value instanceof Collection) ? ((Collection<?>) value).toArray() : null;
        Assert.isTrue(ArrayUtil.isNotEmpty(array), "value of in/not_in condition should be an array or collection and cannot be empty");
        return array;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void toSql(GeneralSql<?> sql) {
        if (value instanceof Sql) {
            sql.append(field).append(op.str()).append("(").append((Sql) value).append(")");
            return;
        }
        switch (op) {
            case EQ:
                if (value == null) sql.append(field).append(" IS NULL");
                else if (!rangeSql(sql)) sql.append(field).append("=?").addParam(enumCheck(value));
                break;
            case NE:
                if (value == null) sql.append(field).append("IS NOT NULL");
                else sql.append(field).append("!=?").addParam(enumCheck(value));
                break;
            case LIKE:
            case NOT_LIKE:
                sql.append(field).append(op.str()).append("?").addParam(value);
                break;
            case IN:
                inSql(sql, Op.EQ);
                break;
            case NOT_IN:
                inSql(sql, Op.NE);
                break;
            default:
                sql.append(field).append(op.str()).append("?").addParam(enumCheck(value));
                break;
        }
    }

    private boolean rangeSql(GeneralSql<?> sql) {
        Range<?> range = paramToRange();
        if (range == null)
            return false;
        range.regular();
        sql.append(field);
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
            case EQ:
                Assert.notNull(value, "condition value should not be null");
                if (!rangeNamedSql(sql)) {
                    sql.append(field).append("=:").append(field).setParam(field, value);
                }
                break;
            case IN:
            case NOT_IN:
                throw new RuntimeException("Named Sql does not support in operator");
            default:
                sql.append(field).append(op.str()).append(":").append(field).setParam(field, value);
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
                .setParam(field, range.getFrom()).setParam(field + "_T", range.getTo());
        return true;
    }

    private void inSql(GeneralSql<?> sql, Op singleOp) {
        Object[] array = (Object[]) value;
        Object[] finalArray = Arrays.stream(array).filter(Objects::nonNull).map(DbaUtil::enumCheck).toArray();
        boolean hasNull = finalArray.length != array.length;
        if (finalArray.length == 0) {
            if (hasNull) sql.append(field).append(op == Op.IN ? " IS NULL" : " IS NOT NULL");
            return;
        }
        hasNull = hasNull && op == Op.IN; // 只有 IN 的时候 才拼 is null 条件， NOT_IN 没有意义
        if (hasNull) sql.append("(");
        if (finalArray.length == 1) {
            sql.append(field).append(singleOp.str()).append("?").addParam(finalArray[0]);
        } else {
            sql.append(field).append(op.str()).append("(").repeat("?", finalArray.length).append(")")
                    .addParam(finalArray);
        }
        if (hasNull) sql.append(DbaUtil.OR).append(field).append(" IS NULL").append(")");
    }

    @Override
    public String toString() {
        return "{" + field + " " + op + " " + value + "}";
    }

}



package io.github.jinghui70.rainbow.dbaccess.cnd;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.sql.Range;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;

import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.enumCheck;

/**
 * 描述一个查询条件的对象，条件的三要素：字段名、比较符、条件值
 */
public class Cnd {

    private String field;

    private Op op = Op.EQ;

    private Object value;

    private List<Cnd> children;

    /**
     * 无参构造函数
     */
    public Cnd() {
    }

    /**
     * 构造一个条件
     *
     * @param field 字段名
     * @param op    比较符
     * @param value 条件值
     */
    private Cnd(String field, Op op, Object value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

     /**
     * 构造一个复合条件，这时候 field 保存的是 逻辑运算符，例如：AND、OR
     *
     * @param field    逻辑运算符，例如：AND、OR
     * @param children 子条件列表
     */
    private Cnd(String field, List<Cnd> children) {
        this.field = field;
        this.children = children;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getField() {
        return field;
    }

    /**
     * 设置字段名
     *
     * @param field 字段名
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * 获取比较运算符
     *
     * @return 比较运算符
     */
    public Op getOp() {
        return op;
    }

    /**
     * 设置比较运算符
     *
     * @param op 比较运算符
     */
    public void setOp(Op op) {
        this.op = op;
    }

    /**
     * 获取条件值
     *
     * @return 条件值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置条件值
     *
     * @param value 条件值
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 获取子条件列表
     *
     * @return 子条件列表
     */
    public List<Cnd> getChildren() {
        return children;
    }

    /**
     * 设置子条件列表
     *
     * @param children 子条件列表
     */
    public void setChildren(List<Cnd> children) {
        this.children = children;
    }

    /**
     * 根据运算符类型处理LIKE条件值
     *
     * @return 处理后的LIKE条件值
     */
    private String likeValue() {
        String str = value.toString();
        return switch (op) {
            case LIKE_LEFT,
                 NOT_LIKE_LEFT -> value + "%";
            case LIKE_RIGHT, NOT_LIKE_RIGHT -> "%" + value;
            default -> {
                if (str.startsWith("%") || str.endsWith("%")) {
                    yield str;
                } else
                    yield "%" + str + "%";
            }
        };
    }

    /**
     * 处理IN/NOT IN条件的值，将其转换为数组
     *
     * @return 条件值数组
     */
    private Object[] inValue() {
        Assert.notNull(value, "value of in/not_in condition cannot be null");
        Object[] array = ArrayUtil.isArray(value)
                ? (Object[]) value
                : (value instanceof Collection) ? ((Collection<?>) value).toArray() : null;
        Assert.isTrue(ArrayUtil.isNotEmpty(array), "value of in/not_in condition should be an array or collection and cannot be empty");
        return array;
    }

    /**
     * 将条件转换为SQL语句
     *
     * @param sql SQL对象，用于拼接SQL语句
     */
    public void toSql(Sql sql) {
        if (children != null) {
            sql.append("(");
            for (Cnd cnd : children) {
                cnd.toSql(sql);
                sql.appendTemp(field);
            }
            sql.clearTemp().append(")");
            return;
        }
        if (value != null && value instanceof Sql) {
            sql.append(field).append(op.str()).append("(").append((Sql) value).append(")");
            return;
        }
        switch (op) {
            case EQ:
                if (rangeSql(sql)) return;
                if (value == null) {
                    sql.append(field).append(Op.IS_NULL.str());
                } else if (ArrayUtil.isArray(value) || value instanceof Collection) {
                    inSql(sql, Op.IN, inValue());
                } else
                    sql.append(field).append("=?").addParam(enumCheck(value));
                break;
            case NE:
                if (value == null) {
                    sql.append(field).append(Op.IS_NOT_NULL.str());
                } else if (ArrayUtil.isArray(value) || value instanceof Collection) {
                    inSql(sql, Op.NOT_IN, inValue());
                } else
                    sql.append(field).append("!=?").addParam(enumCheck(value));
                break;
            case LIKE:
            case NOT_LIKE:
            case LIKE_LEFT:
            case NOT_LIKE_LEFT:
            case LIKE_RIGHT:
            case NOT_LIKE_RIGHT:
                sql.append(field).append(op.str()).append("?").addParam(likeValue());
                break;
            case IN:
            case NOT_IN:
                inSql(sql, op, inValue());
                break;
            case IS_NULL:
            case IS_NOT_NULL:
                sql.append(field).append(op.str());
                break;
            default:
                sql.append(field).append(op.str()).append("?").addParam(enumCheck(value));
                break;
        }
    }

    /**
     * 处理范围查询的SQL拼接
     *
     * @param sql SQL对象
     * @return 是否为范围查询
     */
    private boolean rangeSql(Sql sql) {
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

    /**
     * 将参数转换为Range对象
     *
     * @return Range对象，若无法转换则返回null
     */
    private Range<?> paramToRange() {
        if (value instanceof Map) {
            return BeanUtil.toBeanIgnoreCase(value, Range.class, false);
        } else if (value instanceof Range) {
            return (Range<?>) value;
        } else
            return null;
    }

    /**
     * 处理IN/NOT IN条件的SQL拼接
     *
     * @param sql    SQL对象
     * @param useOp  使用的运算符
     * @param array  条件值数组
     */
    private void inSql(Sql sql, Op useOp, Object[] array) {
        Object[] finalArray = Arrays.stream(array).filter(Objects::nonNull).map(DbaUtil::enumCheck).toArray();
        boolean hasNull = finalArray.length != array.length;
        if (finalArray.length == 0) {
            if (hasNull) sql.append(field).append(useOp == Op.IN ? " IS NULL" : " IS NOT NULL");
            return;
        }
        hasNull = hasNull && useOp == Op.IN; // 只有 IN 的时候 才拼 is null 条件， NOT_IN 没有意义
        if (hasNull) sql.append("(");
        if (finalArray.length == 1) {
            String opStr = useOp == Op.IN ? Op.EQ.str() : Op.NE.str();
            sql.append(field).append(opStr).append("?").addParam(finalArray[0]);
        } else {
            sql.append(field).append(useOp.str()).append("(").repeat("?", finalArray.length, StrUtil.COMMA).append(")")
                    .addParam(finalArray);
        }
        if (hasNull) sql.append(DbaUtil.OR).append(field).append(" IS NULL").append(")");
    }

    /**
     * 返回条件的字符串表示
     *
     * @return 条件的字符串表示
     */
    @Override
    public String toString() {
        return "{" + field + " " + op + " " + value + "}";
    }

    /**
     * 创建AND复合条件
     *
     * @param cnds 条件数组
     * @return AND复合条件，若没有有效条件则返回null
     */
    public static Cnd and(Cnd... cnds) {
        List<Cnd> children = Arrays.stream(cnds).filter(Objects::nonNull).toList();
        return and(children);
    }

    /**
     * 创建AND复合条件
     *
     * @param cnds 条件列表
     * @return AND复合条件，若没有有效条件则返回null
     */
    public static Cnd and(List<Cnd> cnds) {
        return switch (cnds.size()) {
            case 0 -> null;
            case 1 -> cnds.get(0);
            default -> new Cnd(DbaUtil.AND, cnds);
        };
    }

    /**
     * 创建OR复合条件
     *
     * @param cnds 条件数组
     * @return OR复合条件，若没有有效条件则返回null
     */
    public static Cnd or(Cnd... cnds) {
        List<Cnd> children = Arrays.stream(cnds).filter(Objects::nonNull).toList();
        return or(children);
    }

    /**
     * 创建OR复合条件
     *
     * @param cnds 条件列表
     * @return OR复合条件，若没有有效条件则返回null
     */
    public static Cnd or(List<Cnd> cnds) {
        return switch (cnds.size()) {
            case 0 -> null;
            case 1 -> cnds.get(0);
            default -> new Cnd(DbaUtil.OR, cnds);
        };
    }

    /**
     * 创建IS NULL条件
     *
     * @param field 字段名
     * @return IS NULL条件
     */
    public static Cnd isNull(String field) {
        return new Cnd(field, Op.IS_NULL, null);
    }

    /**
     * 创建IS NOT NULL条件
     *
     * @param field 字段名
     * @return IS NOT NULL条件
     */
    public static Cnd isNotNull(String field) {
        return new Cnd(field, Op.IS_NOT_NULL, null);
    }

    /**
     * 创建等于条件
     *
     * @param field 字段名
     * @param value 条件值
     * @return 条件对象
     */
    public static Cnd where(String field, Object value) {
        if (Op.IS_NULL.equals(value) || Op.IS_NOT_NULL.equals(value))
            return new Cnd(field, (Op) value, null);
        return new Cnd(field, Op.EQ, value);
    }

    /**
     * 创建条件
     *
     * @param field 字段名
     * @param op    比较运算符
     * @param value 条件值
     * @return 条件对象
     */
    public static Cnd where(String field, Op op, Object value) {
        return new Cnd(field, op, value);
    }

    /**
     * 根据条件创建等于条件
     *
     * @param condition 是否创建条件
     * @param field     字段名
     * @param value     条件值
     * @return 条件对象，若condition为false则返回null
     */
    public static Cnd where(boolean condition, String field, Object value) {
        return (condition) ? Cnd.where(field, value) : null;
    }

    /**
     * 根据条件创建条件
     *
     * @param condition 是否创建条件
     * @param field     字段名
     * @param op        比较运算符
     * @param value     条件值
     * @return 条件对象，若condition为false则返回null
     */
    public static Cnd where(boolean condition, String field, Op op, Object value) {
        return (condition) ? Cnd.where(field, op, value) : null;
    }

}



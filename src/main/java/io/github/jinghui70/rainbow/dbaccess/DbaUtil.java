package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.SqlUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.CodeEnum;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DbaUtil {

    public static final String MERGE = "merge";

    public static final String INSERT = "insert";

    /**
     * 根据一个对象的类，得到它对应的数据表名
     *
     * @param clazz 对象类
     * @return 数据表名
     */
    public static String tableName(Class<?> clazz) {
        Table entityAnnotation = clazz.getAnnotation(Table.class);
        return entityAnnotation == null ? StrUtil.toUnderlineCase(clazz.getSimpleName()) :
                entityAnnotation.name();
    }

    /**
     * 根据一个对象的类定义，找到所有的主键对应的字段名
     *
     * @param clazz 对象类
     * @return 主键字段名列表
     */
    public static List<String> keyProps(Class<?> clazz) {
        List<String> keys = BeanUtil.getBeanDesc(clazz).getProps().stream()
                .map(PropDesc::getField)
                .filter(field -> field.getAnnotation(Id.class) != null)
                .map(field -> {
                    Column fieldAnnotation = field.getAnnotation(Column.class);
                    return fieldAnnotation == null ? StrUtil.toUnderlineCase(field.getName()) :
                            fieldAnnotation.name();
                }).collect(Collectors.toList());
        Assert.isTrue(keys.size() > 0, "no key field set of {}", clazz);
        return keys;
    }

    /**
     * 检查参数是否是枚举，枚举默认用取值ordinal()，除非它有code()函数
     *
     * @param value 值
     * @return 检查后的值
     */
    public static Object enumCheck(Object value) {
        if (value == null || !value.getClass().isEnum()) return value;
        if (value instanceof CodeEnum)
            return ((CodeEnum) value).code();
        return ((Enum<?>) value).ordinal();
    }

    /**
     * 检查一个数组参数是否是枚举
     *
     * @param arr 原数组
     * @return 处理后数组
     */
    public static Object[] enumCheck(Object[] arr) {
        if (arr == null || arr.length == 0) return arr;
        Class<?> c = arr[0].getClass();
        if (!c.isEnum()) return arr;
        boolean hasCode = CodeEnum.class.isAssignableFrom(c);
        Object[] result = new Object[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = hasCode ? ((CodeEnum) arr[i]).code() : ((Enum<?>) arr[i]).ordinal();
        }
        return result;
    }

    public static void setParameterValue(PreparedStatement ps, int paramIndex,
                                         Object inValue,
                                         Map<Integer, Integer> nullTypeCache) throws SQLException {
        // 空处理
        if (inValue == null) {
            setParameterNull(ps, paramIndex, nullTypeCache);
            return;
        }

        if (inValue instanceof FieldValue) {
            ((FieldValue) inValue).setValue(ps, paramIndex);
            return;
        }

        // 日期特殊处理，默认按照时间戳传入，避免毫秒丢失
        if (inValue instanceof java.util.Date) {
            if (inValue instanceof java.sql.Date) {
                ps.setDate(paramIndex, (java.sql.Date) inValue);
            } else if (inValue instanceof java.sql.Time) {
                ps.setTime(paramIndex, (java.sql.Time) inValue);
            } else {
                ps.setTimestamp(paramIndex, SqlUtil.toSqlTimestamp((java.util.Date) inValue));
            }
            return;
        }

        // 针对大数字类型的特殊处理
        if (inValue instanceof Number) {
            if (inValue instanceof BigDecimal) {
                // BigDecimal的转换交给JDBC驱动处理
                ps.setBigDecimal(paramIndex, (BigDecimal) inValue);
                return;
            }
            if (inValue instanceof BigInteger) {
                // BigInteger转为BigDecimal
                ps.setBigDecimal(paramIndex, new BigDecimal((BigInteger) inValue));
                return;
            }
            // 忽略其它数字类型，按照默认类型传入
        }
        // 其它参数类型
        ps.setObject(paramIndex, inValue);
    }


    private static void setParameterNull(PreparedStatement ps, int paramIndex,
                                         Map<Integer, Integer> nullTypeCache) throws SQLException {
        Integer type = (null == nullTypeCache) ? null : nullTypeCache.get(paramIndex);
        if (null == type) {
            type = StatementUtil.getTypeOfNull(ps, paramIndex);
            if (null != nullTypeCache) {
                nullTypeCache.put(paramIndex, type);
            }
        }
        ps.setNull(paramIndex, type);
    }

}
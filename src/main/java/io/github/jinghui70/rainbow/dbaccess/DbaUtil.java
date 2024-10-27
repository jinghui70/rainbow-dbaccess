package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.SqlUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.CodeEnum;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.OrdinalEnum;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class DbaUtil {

    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";

    public static final String MERGE_INTO = "MERGE INTO ";
    public static final String INSERT_INTO = "INSERT INTO ";

    public static final String ORDER_BY = " ORDER BY ";
    public static final String GROUP_BY = " GROUP BY ";

    public static final String LIKE = " LIKE ";
    public static final String NOT_LIKE = " NOT LIKE ";

    /**
     * 根据一个对象的类，得到它对应的数据表名
     *
     * @param clazz 对象类
     * @return 数据表名
     */
    public static String tableName(Class<?> clazz) {
        Table entityAnnotation = clazz.getAnnotation(Table.class);
        return entityAnnotation == null ? StrUtil.toUnderlineCase(clazz.getSimpleName()).toUpperCase() :
                entityAnnotation.name();
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
        if (value instanceof OrdinalEnum)
            return ((Enum<?>) value).ordinal();
        return ((Enum<?>) value).name();
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
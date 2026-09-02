package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.SqlUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.object.CodeEnum;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库访问工具类，提供表名推导、字段校验、主键提取、枚举转换和 {@link PreparedStatement} 参数设值等底层能力。
 */
public abstract class DbaUtil {

    /** SELECT 关键字 */
    public static final String SELECT = "SELECT ";

    /** WHERE 关键字 */
    public static final String WHERE = " WHERE ";

    /** AND 连接符 */
    public static final String AND = " AND ";

    /** OR 连接符 */
    public static final String OR = " OR ";

    /** MERGE INTO 关键字 */
    public static final String MERGE_INTO = "MERGE INTO ";

    /** INSERT INTO 关键字 */
    public static final String INSERT_INTO = "INSERT INTO ";

    /** ORDER BY 关键字 */
    public static final String ORDER_BY = " ORDER BY ";

    /** 排序方向 */
    public static final String DESC = " DESC";

    /** GROUP BY 关键字 */
    public static final String GROUP_BY = " GROUP BY ";

    /** LIKE 关键字 */
    public static final String LIKE = " LIKE ";

    /** NOT LIKE 关键字 */
    public static final String NOT_LIKE = " NOT LIKE ";

    /**
     * 生成降序排序表达式。
     *
     * @param field 字段名
     * @return {@code field DESC}
     */
    public static String desc(String field) {
        return field + DESC;
    }

    /**
     * 根据实体类推导表名。
     * <p>
     * 若类标注了 {@link Table} 注解则取注解值；否则将类名转为下划线大写作为表名。
     *
     * @param clazz 实体类
     * @return 表名
     */
    public static String tableName(Class<?> clazz) {
        Table entityAnnotation = AnnotatedElementUtils.findMergedAnnotation(clazz, Table.class);
        return entityAnnotation == null ? StrUtil.toUnderlineCase(clazz.getSimpleName()).toUpperCase() :
                entityAnnotation.name();
    }

    /**
     * 判断字符串是否为合法 SQL 标识符（以字母开头，仅含字母、数字和下划线）。
     *
     * @param name 待检验字符串
     * @return 合法返回 {@code true}
     */
    public static boolean isIdentifier(String name) {
        return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    private static final String INVALID_TABLE_NAME = "非法的数据表名:{}";

    /**
     * 校验表名合法性，支持 {@code table}、{@code schema.table}、{@code catalog.schema.table} 格式。
     * <p>
     * 每段必须为合法 SQL 标识符，否则抛出 {@link IllegalArgumentException}。
     *
     * @param tableName 表名
     * @return 校验通过的表名
     * @throws IllegalArgumentException 表名为空或格式非法
     */
    public static String validTableName(String tableName) {
        Assert.notBlank(tableName, "表名不能为空");
        String[] parts = tableName.split("\\.");
        // 允许：table / schema.table / catalog.schema.table
        Assert.isTrue(parts.length >= 1 && parts.length <= 3, INVALID_TABLE_NAME, tableName);
        for (String part : parts) {
            Assert.isTrue(isIdentifier(part), INVALID_TABLE_NAME, tableName);
        }
        return tableName;
    }

    private static final String INVALID_FIELD_NAME = "非法的字段名:{}";

    /**
     * 校验字段名合法性，支持 {@code field} 和 {@code alias.field} 格式。
     * <p>
     * 每段必须为合法 SQL 标识符，否则抛出 {@link IllegalArgumentException}。
     *
     * @param fieldName 字段名
     * @throws IllegalArgumentException 字段名为空或格式非法
     */
    public static void validateFieldName(String fieldName) {
        Assert.notBlank(fieldName, "字段名不能为空");
        String[] parts = fieldName.split("\\.");
        // 允许：field / alias.field
        Assert.isTrue(parts.length >= 1 && parts.length <= 2, INVALID_FIELD_NAME, fieldName);
        for (String part : parts) {
            Assert.isTrue(isIdentifier(part), INVALID_FIELD_NAME, fieldName);
        }
    }

    /**
     * 获取实体类的主键属性列表。
     *
     * @param clazz 实体类，需用 {@code @Id} 标注主键
     * @return 主键属性列表
     * @throws IllegalArgumentException 未定义主键时抛出
     */
    public static List<PropInfo> keyArray(Class<?> clazz) {
        LinkedHashMap<String, PropInfo> propMap = PropInfoCache.get(clazz);
        List<PropInfo> keyArray = propMap.values().stream().filter(p -> p.getId() != null).toList();
        Assert.notEmpty(keyArray, "{} 没有定义主键", clazz.getName());
        return keyArray;
    }

    /**
     * 获取实体类的默认排序字段，以主键字段升序排列。
     * <p>
     * 无主键时返回 {@code null}。
     *
     * @param clazz 实体类
     * @return 默认排序表达式，如 {@code "ID"}；无主键时返回 {@code null}
     */
    public static String defaultOrderBy(Class<?> clazz) {
        LinkedHashMap<String, PropInfo> propMap = PropInfoCache.get(clazz);
        List<PropInfo> keyArray = propMap.values().stream().filter(p -> p.getId() != null).toList();
        if (CollUtil.isNotEmpty(keyArray)) {
            return keyArray.stream().map(PropInfo::getFieldName)
                    .collect(Collectors.joining(StrUtil.COMMA));
        }
        return null;
    }

    /**
     * 枚举值转换：若实现了 {@link CodeEnum} 接口则取 {@code code()}，否则取 {@link Enum#name()}。
     *
     * @param value 待转换值，非枚举类型原样返回
     * @return 转换后的值
     */
    public static Object enumCheck(Object value) {
        if (value == null || !value.getClass().isEnum()) return value;
        if (value instanceof CodeEnum)
            return ((CodeEnum) value).code();
        return ((Enum<?>) value).name();
    }

    /**
     * 为 {@link PreparedStatement} 设置参数值，处理 null、{@link FieldValue}、日期和大数字等特殊类型。
     * <p>
     * null 值通过 {@link #setParameterNull} 设值以确保 JDBC 类型正确。
     * {@link FieldValue} 由其自身完成设值。
     * 日期类型按时间戳传入以避免毫秒丢失。
     * {@link BigInteger} 转为 {@link BigDecimal} 后设值。
     *
     * @param ps             PreparedStatement
     * @param paramIndex     参数位置，从 1 开始
     * @param inValue        参数值
     * @param nullTypeCache  null 类型的缓存，批量执行时复用类型推断结果；可为 {@code null}
     * @throws SQLException 数据库异常
     */
    public static void setParameterValue(PreparedStatement ps, int paramIndex,
                                         Object inValue,
                                         Map<Integer, Integer> nullTypeCache) throws SQLException {
        // 空处理
        if (inValue == null) {
            setParameterNull(ps, paramIndex, nullTypeCache);
            return;
        }

        if (inValue instanceof FieldValue fv) {
            if (fv.getValue() == null)
                setParameterNull(ps, paramIndex, nullTypeCache);
            else
                fv.setParameter(ps, paramIndex);
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


    /**
     * 为 {@link PreparedStatement} 设置 null 参数，通过缓存避免重复推断 SQL 类型。
     *
     * @param ps            PreparedStatement
     * @param paramIndex    参数位置，从 1 开始
     * @param nullTypeCache null 类型的缓存；为 {@code null} 时不缓存
     * @throws SQLException 数据库异常
     */
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

    /**
     * 扩展 Spring 的 {@link ArgumentPreparedStatementSetter}，使用 {@link #setParameterValue} 处理特殊类型参数。
     * <p>
     * 主要用于处理枚举、字段映射器等特殊类型的参数绑定，支持 {@link io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue} 包装类型。
     *
     */
    public static ArgumentPreparedStatementSetter argumentSetter(Collection<?> args) {
        return new ArgumentPreparedStatementSetter(args.toArray()) {
            @Override
            public void doSetValue(@NonNull PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
                setParameterValue(ps, parameterPosition, argValue, null);
            }
        };
    }
}
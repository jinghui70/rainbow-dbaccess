package io.github.jinghui70.rainbow.dbaccess.annotation;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import org.springframework.jdbc.core.SqlTypeValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识实体类属性与数据库表字段的映射关系。
 * <p>
 * 用于自定义字段名、SQL类型、类型转换器等映射配置。
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 数据库字段名。
     * <p>
     * 默认值为 ""，表示将属性名自动转换为小写下划线（snake_case）格式作为字段名，如 userName → user_name。
     * </p>
     *
     * @return 数据库字段名
     */
    String name() default "";

    /**
     * JDBC SQL 类型。
     * <p>
     * 默认值为 {@link SqlTypeValue#TYPE_UNKNOWN}，表示使用自动推断的类型。
     * </p>
     *
     * @return JDBC SQL 类型常量
     * @see java.sql.Types
     */
    int sqlType() default SqlTypeValue.TYPE_UNKNOWN;

    /**
     * 自定义字段类型转换器。
     * <p>
     * 用于在 Java 对象类型与数据库类型之间进行自定义转换。
     * </p>
     *
     * @return 字段转换器类型
     */
    Class<? extends FieldMapper> mapper() default FieldMapper.class;

    /**
     * Blob 字段 JSON 化后是否压缩。
     * <p>
     * 仅对 Blob 类型字段有效，用于控制 JSON 序列化后的数据是否进行压缩存储。
     * </p>
     *
     * @return true 表示压缩，false 表示不压缩，默认为 false
     */
    boolean compress() default false;
}

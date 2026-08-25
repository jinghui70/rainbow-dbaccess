package io.github.jinghui70.rainbow.dbaccess.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识实体类对应的数据库表。
 * <p>
 * 用于自定义表名映射关系。
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 数据库表名。
     * <p>
     * 默认值为 ""，表示将类名自动转换为大写下划线（UPPER_SNAKE_CASE）格式作为表名，如 UserInfo → USER_INFO。
     * </p>
     *
     * @return 数据库表名
     */
    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";
}

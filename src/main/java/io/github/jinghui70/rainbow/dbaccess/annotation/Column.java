package io.github.jinghui70.rainbow.dbaccess.annotation;

import org.springframework.jdbc.core.SqlTypeValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 字段名字，默认为""，就是把属性名转为kebab-case
     *
     * @return 字段名
     */
    String name() default "";

    /**
     * 字段类型
     *
     * @return 字段类型
     */
    int type() default SqlTypeValue.TYPE_UNKNOWN;

}

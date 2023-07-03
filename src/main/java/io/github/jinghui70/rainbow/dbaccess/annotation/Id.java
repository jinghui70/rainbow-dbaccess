package io.github.jinghui70.rainbow.dbaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个属性对应的数据库字段是不是主键
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    // 自增型主键，插入时忽略此字段
    boolean autoIncrement() default false;

}

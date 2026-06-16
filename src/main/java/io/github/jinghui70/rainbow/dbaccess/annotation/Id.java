package io.github.jinghui70.rainbow.dbaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个属性对应的数据库字段是不是主键。
 * <p>
 * 用于标记实体类中作为数据库主键的属性。
 * </p>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    /**
     * 是否为自增型主键。
     * <p>
     * 如果设置为 true，插入数据时会忽略此字段，由数据库自动生成主键值。
     * </p>
     *
     * @return true 表示自增主键，false 表示非自增主键，默认为 false
     */
    boolean autoIncrement() default false;

}

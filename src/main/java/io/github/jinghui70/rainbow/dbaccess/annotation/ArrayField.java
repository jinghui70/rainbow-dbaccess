package io.github.jinghui70.rainbow.dbaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个数组属性对应数据库中多个字段，字段名的规则是 属性名加（下划线）下标
 *
 * @author lijinghui
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ArrayField {

    /**
     * 对应的数组长度
     *
     * @return 数组的长度
     */
    int length();

    /**
     * 数据库中对应该属性下标从0还是1开始
     *
     * @return 下标起始值
     */
    int start() default 0;

    /**
     * 拼名字的时候数字前是否带下划线
     *
     * @return true 带下划线，false 不带
     */
    boolean underline() default false;
}

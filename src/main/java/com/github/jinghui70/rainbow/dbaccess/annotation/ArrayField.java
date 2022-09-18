package com.github.jinghui70.rainbow.dbaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个数组属性对应数据库中多个字段，字段名的规则是 属性名加下划线和下标
 *
 * @author lijinghui
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ArrayField {

    /**
     * 数组字段，数组长度
     *
     * @return
     */
    int length();

    /**
     * 数据库中对应该属性下标从0还是1开始
     *
     * @return
     */
    int start() default 0;

    /**
     * 拼名字的时候数字前是否带下划线
     *
     * @return
     */
    boolean underline() default false;
}

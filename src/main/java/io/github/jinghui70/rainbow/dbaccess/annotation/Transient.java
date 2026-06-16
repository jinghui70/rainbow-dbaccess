package io.github.jinghui70.rainbow.dbaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识实体类属性不映射到数据库表字段。
 * <p>
 * 被标注的属性将被忽略，不参与数据库的读写操作。
 * </p>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}

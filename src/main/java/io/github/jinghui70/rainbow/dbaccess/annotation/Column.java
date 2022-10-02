package io.github.jinghui70.rainbow.dbaccess.annotation;

import org.springframework.jdbc.core.SqlTypeValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLType;
import java.sql.Types;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

    int type() default SqlTypeValue.TYPE_UNKNOWN;

}

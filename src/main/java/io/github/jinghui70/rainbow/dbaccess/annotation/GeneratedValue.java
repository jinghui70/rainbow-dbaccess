package io.github.jinghui70.rainbow.dbaccess.annotation;

import cn.hutool.core.util.StrUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个属性在插入时，若值为 {@code null} 则由指定的生成器自动生成并回填。
 * <p>
 * 生成逻辑由 {@code strategy} 指定的 {@link io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator}
 * 实现，内置 {@code default}（雪花 id）和 {@code now}（当前时间）两种策略，
 * 用户也可注册为 Spring Bean 提供自定义策略。
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {

    /**
     * 生成策略名，对应已注册的 {@link io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator#getName()}。
     *
     * @return 策略名，默认 {@code default}
     */
    String strategy() default "default";

    /**
     * 传递给生成器的参数，含义由具体策略决定（如 {@code default} 视其为字符串主键前缀，
     * {@code now} 视其为日期格式）。
     *
     * @return 参数，默认空字符串
     */
    String param() default StrUtil.EMPTY;
}

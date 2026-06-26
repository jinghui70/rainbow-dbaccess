package io.github.jinghui70.rainbow.dbaccess.valuegen;

/**
 * 字段值生成器：插入时为标注了
 * {@link io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue} 且值为 {@code null}
 * 的属性生成值。
 * <p>
 * 实现类通过 {@link ValueGeneratorRegistry} 注册（Spring 环境下注册为 Bean 即由
 * {@link io.github.jinghui70.rainbow.dbaccess.DbaAutoConfiguration} 自动注册）。
 * 实现应是无状态、线程安全的。
 */
public interface ValueGenerator {

    /**
     * 生成器名称，对应 {@link io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue#strategy()}，
     * 注册时作为唯一键。
     *
     * @return 策略名
     */
    String getName();

    /**
     * 生成字段值。
     *
     * @param context 生成上下文，含 dba、行对象、目标字段和参数
     * @return 生成的值，类型应与目标字段兼容
     */
    Object generate(GenerateContext context);

}

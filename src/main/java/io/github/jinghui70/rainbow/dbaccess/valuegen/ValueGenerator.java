package io.github.jinghui70.rainbow.dbaccess.valuegen;

import org.springframework.beans.factory.InitializingBean;

/**
 * 字段值生成器：插入时为标注了
 * {@link io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue} 且值为 {@code null}
 * 的属性生成值。
 * <p>
 * 本接口继承 {@link InitializingBean} 并提供默认实现：注册为 Spring Bean 时，
 * 容器在初始化阶段会自动调用 {@link #afterPropertiesSet()} 将自身注册到
 * {@link ValueGeneratorRegistry}，与自动配置是否生效无关。
 * 非 Spring 环境下可通过 {@link ValueGeneratorRegistry#register} 手动注册。
 * 实现应是无状态、线程安全的。
 */
public interface ValueGenerator extends InitializingBean {

    /**
     * 将当前生成器注册到 {@link ValueGeneratorRegistry}，
     * 以 {@link #getName()} 为键，同名覆盖。
     * <p>
     * Spring 环境下由容器在 Bean 初始化时自动调用，实现类通常无需覆盖；
     * 覆盖时应调用 {@code super} 或自行保证注册语义。
     */
    @Override
    default void afterPropertiesSet() {
        ValueGeneratorRegistry.register(this);
    }

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

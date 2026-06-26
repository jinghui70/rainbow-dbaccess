package io.github.jinghui70.rainbow.dbaccess.valuegen;

import cn.hutool.core.lang.Assert;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ValueGenerator} 全局注册表。
 * <p>
 * 内置 {@code default} 和 {@code now} 两种策略，首次使用时按需创建并缓存；
 * 用户自定义生成器通过 {@link #register} 注册（Spring 环境下由
 * {@link io.github.jinghui70.rainbow.dbaccess.DbaAutoConfiguration} 自动完成）。
 */
public class ValueGeneratorRegistry {

    private static final Map<String, ValueGenerator> map = new ConcurrentHashMap<>();

    /**
     * 获取指定策略的生成器。内置策略首次访问时惰性创建。
     *
     * @param strategy 策略名
     * @return 对应的生成器，找不到时抛出异常
     * @throws IllegalArgumentException 没有注册该策略时
     */
    public static ValueGenerator get(String strategy) {
        ValueGenerator result = map.computeIfAbsent(strategy, key -> switch (key) {
            case "default" -> new DefaultGenerator();
            case "now" -> new NowGenerator();
            default -> null; // 找不到匹配的，返回 null（ConcurrentHashMap 不会缓存 null）
        });
        Assert.notNull(result, "没有定义字段数据生成器：{}", strategy);
        return result;
    }

    /**
     * 注册一个生成器，以 {@link ValueGenerator#getName()} 为键，同名覆盖。
     *
     * @param generator 生成器
     */
    public static void register(@NonNull ValueGenerator generator) {
        map.put(generator.getName(), generator);
    }
}

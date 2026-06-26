package io.github.jinghui70.rainbow.dbaccess.valuegen;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 默认的 id 生成器，策略名 {@code default}，基于雪花算法。
 * <p>
 * 字段为 {@code long}/{@code Long} 时返回雪花 id；为 {@link String} 时返回其 36 进制大写形式，
 * 并以 {@code param} 作为前缀。雪花的 workerId/datacenterId 优先取 JVM
 * {@code -D} 参数，其次系统环境变量（{@code SNOWFLAKE_WORKER_ID}/{@code SNOWFLAKE_DATACENTER_ID}），
 * 都没有则为 0。
 */
public class DefaultGenerator implements ValueGenerator {

    private static final String WORKER_ID_KEY = "SNOWFLAKE_WORKER_ID";
    private static final String DATACENTER_ID_KEY = "SNOWFLAKE_DATACENTER_ID";

    // 优先读取 JVM -D 参数，如果没有则读取系统环境变量，如果都没有则使用默认值 0L
    private static final long WORKER_ID = Convert.toLong(
            System.getProperty(WORKER_ID_KEY),
            Convert.toLong(System.getenv(WORKER_ID_KEY), 0L)
    );

    private static final long DATACENTER_ID = Convert.toLong(
            System.getProperty(DATACENTER_ID_KEY),
            Convert.toLong(System.getenv(DATACENTER_ID_KEY), 0L)
    );

    private final Snowflake snowflake = IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID);

    @Override
    public String getName() {
        return "default";
    }

    /**
     * 生成雪花 id。
     *
     * @param context 生成上下文，{@code param} 作为 String 字段的前缀
     * @return {@code Long} 或带前缀的 36 进制字符串
     * @throws IllegalArgumentException 字段类型既非 long/Long 也非 String 时
     */
    @Override
    public Object generate(GenerateContext context) {
        Class<?> fieldType = context.field().getType();
        if (fieldType == String.class) {
            return context.param() + Long.toString(snowflake.nextId(), 36).toUpperCase();
        } else if (fieldType == Long.class || fieldType == long.class) {
            return snowflake.nextId();
        } else {
            throw new IllegalArgumentException("DefaultGenerator 不支持的字段类型: " + fieldType.getName());
        }
    }

}

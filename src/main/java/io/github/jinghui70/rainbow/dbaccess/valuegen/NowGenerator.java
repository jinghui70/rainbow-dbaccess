package io.github.jinghui70.rainbow.dbaccess.valuegen;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 当前时间生成器，策略名 {@code now}。
 * <p>
 * 按目标字段类型返回当前时间：{@link LocalDateTime}、{@link Timestamp}、{@link Date}（含其子类）；
 * 字段为 {@link String} 时按 {@code param} 指定的格式格式化，{@code param} 为空时用
 * {@code yyyy-MM-dd HH:mm:ss}。
 */
public class NowGenerator implements ValueGenerator {

    @Override
    public String getName() {
        return "now";
    }

    /**
     * 生成当前时间值。
     *
     * @param context 生成上下文，{@code param} 作为 String 字段的日期格式
     * @return 与字段类型匹配的当前时间对象或其格式化字符串
     * @throws IllegalArgumentException 字段类型不受支持时
     */
    @Override
    public Object generate(GenerateContext context) {
        Class<?> fieldType = context.field().getType();
        if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (fieldType == Timestamp.class) {
            return new Timestamp(System.currentTimeMillis());
        } else if (Date.class.isAssignableFrom(fieldType)) {
            return new Date();
        } else if (fieldType == String.class) {
            String format = context.param();
            if (StrUtil.isEmpty(format)) format = DatePattern.NORM_DATETIME_PATTERN;
            return LocalDateTimeUtil.format(LocalDateTime.now(), format);
        } else {
            throw new IllegalArgumentException("NowGenerator 不支持的字段类型: " + fieldType.getName());
        }
    }
}

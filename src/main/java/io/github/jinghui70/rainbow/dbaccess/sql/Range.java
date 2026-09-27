package io.github.jinghui70.rainbow.dbaccess.sql;

import cn.hutool.core.lang.Assert;

import java.util.Objects;

/**
 * 值范围对象，from为空表示无下限，to为空表示无上限。不能同时为空
 *
 * @param <T> 值的泛型
 * @author lijinghui
 */
public class Range<T extends Comparable<T>> {

    private T from;

    private T to;

    /** 构造空范围，需后续设置 from 或 to */
    public Range() {
    }

    /**
     * 获取范围下限。
     *
     * @return 下限值，null 表示无下限
     */
    public T getFrom() {
        return from;
    }

    /**
     * 设置范围下限。
     *
     * @param from 下限值，null 表示无下限
     */
    public void setFrom(T from) {
        this.from = from;
    }

    /**
     * 获取范围上限。
     *
     * @return 上限值，null 表示无上限
     */
    public T getTo() {
        return to;
    }

    /**
     * 设置范围上限。
     *
     * @param to 上限值，null 表示无上限
     */
    public void setTo(T to) {
        this.to = to;
    }

    /**
     * 规范化范围：from、to 都非空且顺序颠倒时自动交换；from 为空时校验 to 非空。
     *
     * @throws IllegalArgumentException from 和 to 同时为空时抛出
     */
    public void regular() {
        if (from == null) {
            Assert.notNull(to);
        } else {
            if (to != null && from.compareTo(to) > 0) {
                T mid = from;
                from = to;
                to = mid;
            }
        }
    }

    /**
     * check if the range is full range,means the from and to both is not null
     *
     * @return true if the range is full range,false otherwise
     */
    public boolean fullRange() {
        return from != null && to != null;
    }

    /**
     * check if the range is a single value
     *
     * @return true if the range is single value,false otherwise
     */
    public boolean singleValue() {
        return Objects.equals(to, from);
    }

    /**
     * create a new range
     * @param from from value
     * @param to to value
     * @return range object
     * @param <T> value type
     */
    public static <T extends Comparable<T>> Range<T> of(T from, T to) {
        Range<T> result = new Range<>();
        result.setFrom(from);
        result.setTo(to);
        result.regular();
        return result;
    }
}

package io.github.jinghui70.rainbow.dbaccess;

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

    public Range() {
    }

    public T getFrom() {
        return from;
    }

    public void setFrom(T from) {
        this.from = from;
    }

    public T getTo() {
        return to;
    }

    public void setTo(T to) {
        this.to = to;
    }

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
     * @param <T>
     */
    public static <T extends Comparable<T>> Range<T> of(T from, T to) {
        Range<T> result = new Range<>();
        result.setFrom(from);
        result.setTo(to);
        result.regular();
        return result;
    }
}

package io.github.jinghui70.rainbow.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.function.Function;

/**
 * 增强StringBuild，在连续append的时候处理多余的逗号这样的场景
 *
 * @author lijinghui
 */
@SuppressWarnings("unchecked")
public abstract class StringBuilderWrapper<T extends StringBuilderWrapper<T>> implements Appendable {

    protected StringBuilder sb = new StringBuilder();

    private String tempStr = null;

    public StringBuilderWrapper() {
    }

    public StringBuilderWrapper(String str) {
        this();
        sb.append(str);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * 添加一个对象的toString字符串
     *
     * @param obj 指定对象
     * @return 自己
     */
    public T append(Object obj) {
        if (obj == null) {
            checkTemp();
            return (T) this;
        }
        return this.append(obj.toString());
    }

    @Override
    public T append(char ch) {
        checkTemp();
        sb.append(ch);
        return (T) this;
    }

    @Override
    public T append(CharSequence csq) {
        checkTemp();
        sb.append(csq);
        return (T) this;
    }

    @Override
    public T append(CharSequence csq, int start, int end) {
        checkTemp();
        sb.append(csq, start, end);
        return (T) this;
    }

    public T append(boolean condition, String str) {
        checkTemp();
        if (condition) sb.append(str);
        return (T) this;
    }

    /**
     * 把一个字符串复制多遍，用逗号分隔符，添加进来
     *
     * @param str   字符串
     * @param times 复制次数
     * @return 自己
     */
    public T repeat(String str, int times) {
        return repeat(str, times, StrUtil.COMMA);
    }

    /**
     * 把一个字符串复制多遍，用指定分隔符，添加进来
     *
     * @param str       字符串
     * @param times     复制次数
     * @param delimiter 分隔符
     * @return 自己
     */
    public T repeat(String str, int times, String delimiter) {
        if (times > 0) {
            append(str);
            for (int i = 1; i < times; i++) {
                if (StrUtil.isNotEmpty(delimiter))
                    sb.append(delimiter);
                sb.append(str);
            }
        }
        return (T) this;
    }

    /**
     * 一组对象，转为String，以逗号分隔，添加进来
     *
     * @param objects 字符串集合
     * @return 自己
     */
    public T join(Collection<?> objects) {
        return this.join(objects, StrUtil.COMMA);
    }

    /**
     * 添加一组对象，转为toString字符串
     *
     * @param objects   对象列表
     * @param delimiter 分隔符
     * @return 自己
     */
    public T join(Collection<?> objects, String delimiter) {
        return join(objects, Object::toString, delimiter);
    }

    /**
     * 添加一组对象，转为toString字符串，以逗号分隔
     *
     * @param array 对象列表
     * @param <O>   对象泛型
     * @return 自己
     */
    public <O> T join(O[] array) {
        return join(array, Object::toString, StrUtil.COMMA);
    }

    /**
     * 添加一组对象，转为toString字符串，以指定分隔符分隔
     *
     * @param array     对象列表
     * @param delimiter 分隔符
     * @param <O>       对象泛型
     * @return 自己
     */
    public <O> T join(O[] array, String delimiter) {
        return join(array, Object::toString, delimiter);
    }

    /**
     * 一组对象，转为字符串后添加进来
     *
     * @param list      对象列表
     * @param toString  对象转为字符串的函数
     * @param delimiter 分隔符
     * @param <O>       对象泛型
     * @return 自己
     */
    public <O> T join(Collection<O> list, Function<O, String> toString, String delimiter) {
        if (CollUtil.isNotEmpty(list)) {
            for (O obj : list) {
                append(toString.apply(obj)).appendTemp(delimiter);
            }
            clearTemp();
        }
        return (T) this;
    }

    /**
     * 一组对象，转为字符串后添加进来
     *
     * @param array     对象列表
     * @param toString  对象转为字符串的函数
     * @param delimiter 分隔符
     * @param <O>       对象泛型
     * @return 自己
     */
    public <O> T join(O[] array, Function<O, String> toString, String delimiter) {
        if (ArrayUtil.isNotEmpty(array)) {
            for (O obj : array) {
                append(toString.apply(obj)).appendTemp(delimiter);
            }
            clearTemp();
        }
        return (T) this;
    }

    /**
     * 临时字符串用来对付 Where and 逗号这样时有时无 的字符串，当后续有append的时候，会被自动append进去
     *
     * @param str 添加的字符串
     * @return 自己
     */
    public T appendTemp(String str) {
        checkTemp();
        this.tempStr = str;
        return (T) this;
    }

    public T appendTempComma() {
        return this.appendTemp(StrUtil.COMMA);
    }

    /**
     * 取消temp中存储的字符串
     *
     * @return 自己
     */
    public T clearTemp() {
        this.tempStr = null;
        return (T) this;
    }

    private void checkTemp() {
        if (StrUtil.isNotEmpty(tempStr))
            sb.append(tempStr);
        tempStr = null;
    }

    /**
     * 获取内容长度
     *
     * @return 长度值
     */
    public int length() {
        return sb.length();
    }

    /**
     * 设置新的长度
     *
     * @param newLength 新的长度
     * @return 自己
     */
    public T setLength(int newLength) {
        sb.setLength(newLength);
        return (T) this;
    }

    /**
     * 限制最大长度，如果原来的长度大于设置的长度，截断并添加...
     *
     * @param newLength 新的长度
     * @return 自己
     */
    public T limit(int newLength) {
        if (length() > newLength) {
            setLength(newLength - 3);
            append("...");
        }
        return (T) this;
    }

}

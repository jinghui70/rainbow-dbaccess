package io.github.jinghui70.rainbow.utils;

import cn.hutool.core.collection.CollUtil;
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

    public T append(String str, int times, String delimiter) {
        append(str);
        for (int i = 1; i < times; i++) {
            if (StrUtil.isNotEmpty(delimiter))
                sb.append(delimiter);
            sb.append(str);
        }
        return (T) this;
    }

    public T join(Collection<String> strs) {
        return this.append(strs, StrUtil.COMMA);
    }

    public <O> T append(Collection<O> list, Function<O, String> toString, String delimiter) {
        if (CollUtil.isNotEmpty(list)) {
            for (O obj : list) {
                append(toString.apply(obj)).appendTemp(delimiter);
            }
            clearTemp();
        }
        return (T) this;
    }

    public <O> T append(Collection<O> list, String delimiter) {
        return append(list, Object::toString, delimiter);
    }

    /**
     * 临时字符串用来对付 Where and 逗号这样时有时无 的字符串，当后续有append的时候，会被自动append进去
     *
     * @param str
     * @return
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
     * @return
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

    public int length() {
        return sb.length();
    }

    public T setLength(int newLength) {
        sb.setLength(newLength);
        return (T) this;
    }

    /**
     * 限制最大长度
     *
     * @param newLength
     */
    public T limit(int newLength) {
        if (length() > newLength) {
            setLength(newLength - 3);
            append("...");
        }
        return (T) this;
    }

}

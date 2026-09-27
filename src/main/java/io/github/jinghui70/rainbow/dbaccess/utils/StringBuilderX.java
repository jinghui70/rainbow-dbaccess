package io.github.jinghui70.rainbow.dbaccess.utils;

/**
 * 增强StringBuild，在连续append的时候处理多余的逗号这样的场景
 *
 * @author lijinghui
 */
public class StringBuilderX extends StringBuilderWrapper<StringBuilderX> {

    /** 构造空的字符串构建器 */
    public StringBuilderX() {
        super();
    }

    /**
     * 以指定字符串为初始内容构造。
     *
     * @param str 初始内容
     */
    public StringBuilderX(String str) {
        super(str);
    }

}

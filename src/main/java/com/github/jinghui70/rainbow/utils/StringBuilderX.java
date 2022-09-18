package com.github.jinghui70.rainbow.utils;

/**
 * 增强StringBuild，在连续append的时候处理多余的逗号这样的场景
 *
 * @author lijinghui
 */
public class StringBuilderX extends StringBuilderWrapper<StringBuilderX> {

    public StringBuilderX() {
        super();
    }

    public StringBuilderX(String str) {
        super(str);
    }

}

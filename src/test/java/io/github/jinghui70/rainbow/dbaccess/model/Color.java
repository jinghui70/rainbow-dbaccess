package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.object.CodeEnum;

/**
 * CodeEnum 枚举 — 存 code() 到数据库。
 */
public enum Color implements CodeEnum {
    RED("R"),
    GREEN("G"),
    BLUE("B");

    private final String code;

    Color(String code) { this.code = code; }

    @Override
    public String code() { return code; }
}

package io.github.jinghui70.rainbow.dbaccess.cnd;

import io.github.jinghui70.rainbow.dbaccess.DbaUtil;

public enum Op {

    EQ("="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE(DbaUtil.LIKE),
    LIKE_LEFT(DbaUtil.LIKE),
    LIKE_RIGHT(DbaUtil.LIKE),
    NOT_LIKE(DbaUtil.NOT_LIKE),
    NOT_LIKE_LEFT(DbaUtil.NOT_LIKE),
    NOT_LIKE_RIGHT(DbaUtil.NOT_LIKE),
    IN(" IN "),
    NOT_IN(" NOT IN ");

    private final String op;

    public String str() {
        return this.op;
    }

    Op(String op) {
        this.op = op;
    }
}

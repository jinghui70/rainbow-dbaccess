package io.github.jinghui70.rainbow.dbaccess.cnd;

public enum Op {

    EQ("="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE(" like "),
    LIKE_LEFT(" like "),
    LIKE_RIGHT(" like "),
    NOT_LIKE(" not like "),
    NOT_LIKE_LEFT(" not like "),
    NOT_LIKE_RIGHT(" not like "),
    IN(" in "),
    NOT_IN(" not in ");

    private final String op;

    public String str() {
        return this.op;
    }

    Op(String op) {
        this.op = op;
    }
}

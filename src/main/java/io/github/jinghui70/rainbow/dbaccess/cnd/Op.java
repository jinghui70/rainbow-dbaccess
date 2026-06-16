package io.github.jinghui70.rainbow.dbaccess.cnd;

import io.github.jinghui70.rainbow.dbaccess.DbaUtil;

/**
 * SQL条件比较运算符枚举
 */
public enum Op {

    /** 等于 (=) */
    EQ("="),

    /** 不等于 (!=) */
    NE("!="),

    /** 大于 (&gt;) */
    GT(">"),

    /** 大于等于 (&gt;=) */
    GE(">="),

    /** 小于 (&lt;) */
    LT("<"),

    /** 小于等于 (&lt;=) */
    LE("<="),

    /** 模糊匹配 (LIKE)，自动在值两端添加 % */
    LIKE(DbaUtil.LIKE),

    /** 左模糊匹配 (LIKE)，自动在值右侧添加 % */
    LIKE_LEFT(DbaUtil.LIKE),

    /** 右模糊匹配 (LIKE)，自动在值左侧添加 % */
    LIKE_RIGHT(DbaUtil.LIKE),

    /** 不模糊匹配 (NOT LIKE)，自动在值两端添加 % */
    NOT_LIKE(DbaUtil.NOT_LIKE),

    /** 不左模糊匹配 (NOT LIKE)，自动在值右侧添加 % */
    NOT_LIKE_LEFT(DbaUtil.NOT_LIKE),

    /** 不右模糊匹配 (NOT LIKE)，自动在值左侧添加 % */
    NOT_LIKE_RIGHT(DbaUtil.NOT_LIKE),

    /** 在范围内 (IN) */
    IN(" IN "),

    /** 不在范围内 (NOT IN) */
    NOT_IN(" NOT IN "),

    /** 为空 (IS NULL) */
    IS_NULL(" IS NULL"),

    /** 不为空 (IS NOT NULL) */
    IS_NOT_NULL(" IS NOT NULL");

    private final String op;

    /**
     * 获取运算符的字符串表示
     *
     * @return 运算符字符串
     */
    public String str() {
        return this.op;
    }

    /**
     * 构造函数
     *
     * @param op 运算符字符串
     */
    Op(String op) {
        this.op = op;
    }
}

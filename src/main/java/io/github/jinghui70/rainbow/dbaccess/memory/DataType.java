package io.github.jinghui70.rainbow.dbaccess.memory;

/**
 * 数据库字段数据类型枚举
 * <p>
 * 定义了内存数据库支持的各种数据类型
 * </p>
 */
public enum DataType {
    /** 1 字节整数 */
    TINYINT,
    /** 2 字节整数 */
    SMALLINT,
    /** 4 字节整数 */
    INT,
    /** 8 字节整数 */
    BIGINT,
    /** 双精度浮点数 */
    DOUBLE,
    /** 定点数，需指定精度和小数位 */
    NUMERIC,
    /** 日期，不含时间 */
    DATE,
    /** 时间，不含日期 */
    TIME,
    /** 日期时间 */
    TIMESTAMP,
    /** 定长字符串 */
    CHAR,
    /** 变长字符串，需指定长度 */
    VARCHAR,
    /** 字符大对象 */
    CLOB,
    /** 二进制大对象 */
    BLOB
}

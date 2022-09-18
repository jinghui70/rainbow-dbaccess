package com.github.jinghui70.rainbow.dbaccess.memory;

import java.math.BigDecimal;

public enum DataType {
    SMALLINT, INT, LONG, DOUBLE, NUMERIC, DATE, TIME, TIMESTAMP, CHAR, VARCHAR, CLOB, BLOB;

    public static Class<?> dataClass(DataType type) {
        switch (type) {
            case SMALLINT:
                return Short.class;
            case INT:
                return Integer.class;
            case LONG:
                return Long.class;
            case DOUBLE:
                return Double.class;
            case NUMERIC:
                return BigDecimal.class;
            case DATE:
                return java.sql.Date.class;
            case TIME:
                return java.sql.Time.class;
            case TIMESTAMP:
                return java.sql.Timestamp.class;
            case BLOB:
                return byte[].class;
            default:
                return String.class;
        }
    }
}

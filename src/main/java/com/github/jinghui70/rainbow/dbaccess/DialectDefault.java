package com.github.jinghui70.rainbow.dbaccess;

/**
 * 默认数据库方言接口，支持H2, MySql
 *
 * @author lijinghui
 */
public class DialectDefault implements Dialect {

    @Override
    public String wrapLimitSql(String sql, int limit) {
        return String.format("%s LIMIT %d", sql, limit);
    }

    @Override
    public String wrapPagedSql(String sql, int pageNo, int pageSize) {
        int from = (pageNo - 1) * pageSize + 1;
        return String.format("%s LIMIT %d, %d", sql, from - 1, pageSize);
    }

    @Override
    public String wrapRangeSql(String sql, int from, int to) {
        return String.format("%s LIMIT %d, %d", sql, from - 1, to - from + 1);
    }

    public static Dialect INSTANCE = new DialectDefault();
}
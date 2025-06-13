package io.github.jinghui70.rainbow.dbaccess.dialect;

/**
 * PostgreSQL方言实现
 *
 * @author lijinghui
 */
public class DialectPostgreSQL extends DialectDefault {


    @Override
    public String wrapPagedSql(String sql, int pageNo, int pageSize) {
        int from = (pageNo - 1) * pageSize + 1;
        return String.format("%s LIMIT %d OFFSET %d", sql, pageSize, from - 1);
    }

    @Override
    public String wrapRangeSql(String sql, int from, int to) {
        return String.format("%s LIMIT %d OFFSET %d", sql, to - from + 1, from - 1);
    }

}
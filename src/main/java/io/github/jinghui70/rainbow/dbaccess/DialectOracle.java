package io.github.jinghui70.rainbow.dbaccess;

/**
 * Oracle数据库方言接口
 *
 * @author lijinghui
 */
public class DialectOracle implements Dialect {

    @Override
    public String wrapLimitSql(String sql, int limit) {
        return String.format("select * from (%s) where ROWNUM<=%d", sql, limit);
    }

    @Override
    public String wrapPagedSql(String sql, int pageNo, int pageSize) {
        int from = (pageNo - 1) * pageSize + 1;
        int to = pageNo * pageSize;
        return wrapRangeSql(sql, from, to);
    }

    @Override
    public String wrapRangeSql(String sql, int from, int to) {
        return String.format("select * from (select A.*,ROWNUM AS RN from (%s) A where ROWNUM <=%d) where RN>=%d", sql,
                to, from);
    }
}
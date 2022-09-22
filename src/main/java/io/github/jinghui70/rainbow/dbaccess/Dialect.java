package io.github.jinghui70.rainbow.dbaccess;

/**
 * 数据库方言接口
 *
 * @author lijinghui
 */
public interface Dialect {

    /**
     * 返回取前几条记录的语句
     *
     * @param sql   要查询的语句
     * @param limit 需要返回的行数
     * @return 返回处理后的sql
     */
    String wrapLimitSql(String sql, int limit);

    /**
     * 返回分页的查询语句
     *
     * @param sql      要查询的语句
     * @param pageNo   当前页数
     * @param pageSize 每页行数
     * @return 返回处理后的sql
     */
    String wrapPagedSql(String sql, int pageNo, int pageSize);


    /**
     * 返回分页的查询语句
     *
     * @param sql  要查询的语句
     * @param from 从第几条
     * @param to   到第几条
     * @return 返回处理后的sql
     */
    String wrapRangeSql(String sql, int from, int to);
}
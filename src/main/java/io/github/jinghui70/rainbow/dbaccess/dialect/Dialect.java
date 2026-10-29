package io.github.jinghui70.rainbow.dbaccess.dialect;

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

    /**
     * 返回删除数据表的语句。
     * <p>
     * 默认返回 {@code DROP TABLE IF EXISTS 表名}，适用于 H2/MySQL/PostgreSQL。
     * 不支持 {@code IF EXISTS} 的数据库（如 Oracle）由子类覆写，需保持"表不存在也不报错"的语义。
     *
     * @param tableName 数据表名
     * @return 删除数据表的 SQL
     */
    default String dropTableSql(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }
}
package io.github.jinghui70.rainbow.dbaccess.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于从 {@link ResultSet} 中提取值的函数式接口。
 * <p>
 * 类似 {@link java.util.function.Function}，专门用于处理 ResultSet 并可能抛出 {@link SQLException}。
 * 主要用于 {@link Sql#queryToMap} 和 {@link Sql#queryToGroup} 等方法中提取 key 或 value。
 *
 * @param <V> 返回值类型
 * @author lijinghui
 * @see Sql#queryToMap
 * @see Sql#queryToGroup
 */
@FunctionalInterface
public interface ResultSetFunction<V> {

    /**
     * 从 ResultSet 中提取值。
     *
     * @param resultSet ResultSet 对象
     * @return 提取的值
     * @throws SQLException SQL 异常
     */
    V apply(ResultSet resultSet) throws SQLException;

}

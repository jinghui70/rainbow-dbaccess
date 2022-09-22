package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.Closeable;
import java.sql.SQLException;

/**
 * 内存表
 *
 * @author lijinghui
 */
public class MemoryDba extends Dba implements Closeable {

    private final MemoryConnection conn;

    public MemoryDba() {
        try {
            conn = new MemoryConnection();
        } catch (SQLException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
        MemoryDataSource ds = new MemoryDataSource(conn);
        jdbcTemplate = new JdbcTemplate(ds);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void close() {
        try {
            this.conn.getRaw().close();
        } catch (SQLException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    public void createTable(Table table) {
        getJdbcTemplate().update(table.ddl());
    }

}

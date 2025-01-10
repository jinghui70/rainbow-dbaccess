package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;

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
        initDataSource(ds, null);
    }

    @Override
    public void close() {
        try {
            this.conn.getRaw().close();
        } catch (SQLException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    /**
     * 创建一个内存表
     *
     * @param table 表对象
     */
    public void createTable(Table table) {
        getJdbcTemplate().update(table.ddl());
    }

    public void createTable(String tableName, Field... fields) {
        Table table = new Table(tableName, fields);
        createTable(table);
    }

    /**
     * 创建缺省名字为X的内存表
     *
     * @param fields 字段列表
     */
    public void createTable(Field... fields) {
        createTable(Table.DEFAULT_NAME, fields);
    }
}

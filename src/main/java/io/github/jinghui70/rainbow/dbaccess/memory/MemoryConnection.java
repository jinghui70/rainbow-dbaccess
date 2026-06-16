package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.db.ds.pooled.ConnectionWraper;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 内存数据库连接
 * <p>
 * 基于H2内存数据库的连接封装，close方法被重写为空操作以保持连接持久
 * </p>
 */
public class MemoryConnection extends ConnectionWraper {

    /**
     * 构造函数，创建H2内存数据库连接
     *
     * @throws SQLException 当H2驱动未找到或连接创建失败时抛出
     */
    public MemoryConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("h2 driver not found");
        }
        raw = DriverManager.getConnection("jdbc:h2:mem:");
    }

    /**
     * 关闭连接（空操作，保持连接不关闭）
     */
    @Override
    public void close() {
    }

    /**
     * 判断连接是否已关闭
     *
     * @return true表示连接已关闭
     * @throws SQLException 当访问数据库状态失败时抛出
     */
    @Override
    public boolean isClosed() throws SQLException {
        return raw.isClosed();
    }

}

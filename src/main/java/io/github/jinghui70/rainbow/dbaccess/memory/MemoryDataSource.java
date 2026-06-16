package io.github.jinghui70.rainbow.dbaccess.memory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * 内存数据库数据源
 * <p>
 * 基于H2内存数据库的DataSource实现，始终返回同一个连接实例
 * </p>
 */
public class MemoryDataSource implements DataSource {

    private final MemoryConnection con;

    /**
     * 构造函数
     *
     * @param con 内存数据库连接
     */
    public MemoryDataSource(MemoryConnection con) {
        this.con = con;
    }

    /**
     * 获取数据库连接
     *
     * @return 内存数据库连接
     */
    @Override
    public Connection getConnection() {
        return con;
    }

    /**
     * 获取数据库连接（忽略用户名密码，始终返回同一个连接）
     *
     * @param username 用户名（忽略）
     * @param password 密码（忽略）
     * @return 内存数据库连接
     */
    @Override
    public Connection getConnection(String username, String password) {
        return con;
    }

    /**
     * 获取父日志记录器
     *
     * @return 始终返回null
     */
    @Override
    public Logger getParentLogger() {
        return null;
    }

    /**
     * 解包为指定类型
     *
     * @param iface 要解包的类型
     * @param <T>   类型参数
     * @return 始终返回null
     */
    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    /**
     * 判断是否是指定类型的包装器
     *
     * @param iface 要检查的类型
     * @return 始终返回false
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    /**
     * 获取日志写入器
     *
     * @return 始终返回null
     */
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    /**
     * 设置日志写入器（空操作）
     *
     * @param out 日志写入器
     */
    @Override
    public void setLogWriter(PrintWriter out) {
    }

    /**
     * 获取登录超时时间
     *
     * @return 始终返回0
     */
    @Override
    public int getLoginTimeout() {
        return 0;
    }

    /**
     * 设置登录超时时间（空操作）
     *
     * @param seconds 超时秒数
     */
    @Override
    public void setLoginTimeout(int seconds) {
    }

}

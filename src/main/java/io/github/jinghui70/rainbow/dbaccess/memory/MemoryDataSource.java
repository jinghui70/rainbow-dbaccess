package io.github.jinghui70.rainbow.dbaccess.memory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

public class MemoryDataSource implements DataSource {

    private final MemoryConnection con;

    public MemoryDataSource(MemoryConnection con) {
        this.con = con;
    }

    @Override
    public Connection getConnection() {
        return con;
    }

    @Override
    public Connection getConnection(String username, String password) {
        return con;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

}

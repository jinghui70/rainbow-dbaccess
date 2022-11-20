package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.db.ds.pooled.ConnectionWraper;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MemoryConnection extends ConnectionWraper {

    public MemoryConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("h2 driver not found");
        }
        raw = DriverManager.getConnection("jdbc:h2:mem:");
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isClosed() throws SQLException {
        return raw.isClosed();
    }

}

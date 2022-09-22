package io.github.jinghui70.rainbow.dbaccess.memory;

import cn.hutool.db.ds.pooled.ConnectionWraper;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MemoryConnection extends ConnectionWraper {

    public MemoryConnection() throws SQLException {
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

package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class ArgumentSetter extends ArgumentPreparedStatementSetter {

    public ArgumentSetter(Collection<?> args) {
        super(args.toArray());
    }

    @Override
    public void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
        DbaUtil.setParameterValue(ps, parameterPosition, argValue, null);
    }
}

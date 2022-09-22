package io.github.jinghui70.rainbow.dbaccess;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetFunction<V>  {

    V apply(ResultSet resultSet) throws SQLException;

}

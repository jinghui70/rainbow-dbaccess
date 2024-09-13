package io.github.jinghui70.rainbow.dbaccess.object;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface BeanMapperPostProcessor<T> {

    void process(T t, ResultSet resultSet) throws SQLException;

}

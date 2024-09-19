package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.mapper.MapRowMapper;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * 封装了一个Sql的内容对象
 *
 * @author lijinghui
 */
public class Sql extends GeneralSql<Sql> {

    protected Sql() {
        super();
    }

    public Sql(String str) {
        super(str);
    }

    public Sql(Dba dba) {
        super(dba);
    }

    public <T> ObjectSql<T> from(Class<T> queryClass) {
        return new ObjectSql<>(dba, queryClass).append(getSql()).from(DbaUtil.tableName(queryClass));
    }

    public List<Map<String, Object>> queryForList() throws DataAccessException {
        return queryForList(MapRowMapper.INSTANCE);
    }

    public static Sql create() {
        return new Sql();
    }

    public static Sql create(String str) {
        return new Sql(str);
    }

}
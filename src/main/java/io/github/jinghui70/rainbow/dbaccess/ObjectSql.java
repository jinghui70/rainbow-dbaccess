package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;

import java.util.List;
import java.util.Optional;

public class ObjectSql<T> extends GeneralSql<ObjectSql<T>> {

    private final Class<T> queryClass;

    public ObjectSql(Dba dba, Class<T> queryClass) {
        super(dba);
        this.queryClass = queryClass;
    }

    public T queryForObject() {
        return queryForObject(queryClass);
    }

    public Optional<T> queryForObjectOptional() {
        return queryForObjectOptional(BeanMapper.of(queryClass));
    }

    public List<T> queryForList() {
        return queryForList(queryClass);
    }
}

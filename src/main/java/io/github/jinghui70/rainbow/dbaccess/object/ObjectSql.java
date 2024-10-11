package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.CaseInsensitiveMap;
import io.github.jinghui70.rainbow.dbaccess.*;
import io.github.jinghui70.rainbow.utils.tree.ITreeNode;
import io.github.jinghui70.rainbow.utils.tree.TreeNode;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

@SuppressWarnings("unused")
public class ObjectSql<T> extends GeneralSql<ObjectSql<T>> {

    private final Class<T> queryClass;
    private final LinkedHashMap<String, PropInfo> propMap;
    private final BeanMapper<T> mapper;

    public ObjectSql(Dba dba, Class<T> queryClass) {
        super(dba);
        this.queryClass = queryClass;
        this.propMap = PropInfoCache.get(queryClass);
        this.mapper = new BeanMapper<>(queryClass, propMap);
    }

    public T queryForObject() {
        return queryForObject(mapper);
    }

    public Optional<T> queryForObjectOptional() {
        return queryForObjectOptional(mapper);
    }

    public List<T> queryForList() {
        return queryForList(mapper);
    }

    public PageData<T> pageQuery(int pageNo, int pageSize) {
        return pageQuery(mapper, pageNo, pageSize);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<T> queryForTree() {
        Assert.isAssignable(TreeNode.class, queryClass);
        return queryForTree((RowMapper<? extends ITreeNode>) mapper);
    }

    public <K> Map<K, T> queryToMap(ResultSetFunction<K> keyFunc) {
        return queryToMap(keyFunc, mapper);
    }

    public <K> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc) {
        return queryToGroup(keyFunc, mapper);
    }

    public ObjectSql<T> appendFields() {
        return join(propMap.keySet());
    }

    public ObjectSql<T> selectFields(Map<String, Object> replaceMap) {
        replaceMap = new CaseInsensitiveMap<>(replaceMap);
        append("SELECT ");
        for (String key : propMap.keySet()) {
            if (replaceMap.containsKey(key))
                append("?").addParam(replaceMap.get(key)).append(" AS ").append(key);
            else
                append(key);
            appendTempComma();
        }
        return clearTemp().from(DbaUtil.tableName(queryClass));
    }

}

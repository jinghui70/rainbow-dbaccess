package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.lang.Assert;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.utils.TreeNode;
import io.github.jinghui70.rainbow.utils.WrapTreeNode;

import java.util.List;
import java.util.Map;
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

    public PageData<T> pageQuery(int pageNo, int pageSize) {
        return pageQuery(queryClass, pageNo, pageSize);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<T> queryForTree() {
        Assert.isAssignable(TreeNode.class, queryClass);
        return queryForTree((Class<? extends TreeNode>) queryClass);
    }

    public List<WrapTreeNode<T>> queryForWrapTree() {
        return queryForWrapTree(queryClass);
    }

    public <K> Map<K, T> queryToMap(ResultSetFunction<K> keyFunc) {
        return queryToMap(keyFunc, queryClass);
    }

    public <K> Map<K, List<T>> queryToGroup(ResultSetFunction<K> keyFunc) {
        return queryToGroup(keyFunc, queryClass);
    }
}

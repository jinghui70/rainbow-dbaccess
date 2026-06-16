package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.collection.CollUtil;

import java.util.List;

/**
 * 树节点接口，定义了树形结构节点的基本操作。
 *
 * @param <T> 节点类型，必须实现ITreeNode接口
 */
public interface ITreeNode<T extends ITreeNode<T>> {

    /**
     * 获取子节点列表。
     *
     * @return 子节点列表
     */
    List<T> getChildren();

    /**
     * 设置子节点列表。
     *
     * @param children 子节点列表
     */
    void setChildren(List<T> children);

    /**
     * 添加一个子节点。
     *
     * @param child 要添加的子节点
     */
    void addChild(T child);

    /**
     * 判断是否有子节点。
     *
     * @return 如果有子节点返回true，否则返回false
     */
    default boolean hasChild() {
        return CollUtil.isNotEmpty(getChildren());
    }
}

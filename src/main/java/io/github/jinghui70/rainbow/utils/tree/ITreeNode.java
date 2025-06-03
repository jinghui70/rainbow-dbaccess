package io.github.jinghui70.rainbow.utils.tree;

import cn.hutool.core.collection.CollUtil;

import java.util.Collection;
import java.util.List;

public interface ITreeNode<T extends ITreeNode<T>> {

    List<T> getChildren();

    void setChildren(List<T> children);

    void addChild(T child);

    default boolean hasChild() {
        return CollUtil.isNotEmpty(getChildren());
    }
}

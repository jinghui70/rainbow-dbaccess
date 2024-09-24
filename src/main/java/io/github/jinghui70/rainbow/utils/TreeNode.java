package io.github.jinghui70.rainbow.utils;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T extends TreeNode<T>> {

    @Transient
    private List<T> children;

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    public void addChild(T child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public boolean hasChild() {
        return CollUtil.isNotEmpty(children);
    }
}

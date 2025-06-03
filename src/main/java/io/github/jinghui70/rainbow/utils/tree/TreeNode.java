package io.github.jinghui70.rainbow.utils.tree;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T extends TreeNode<T>> implements ITreeNode<T> {

    @Transient
    private List<T> children;

    @Override
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

    public void addChildren(List<T> children) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.addAll(children);
    }

}

package io.github.jinghui70.rainbow.dbaccess.tree;

import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;

import java.util.List;

/**
 * 树节点抽象类，实现了ITreeNode接口的基本功能。
 *
 * @param <T> 节点类型，必须继承自TreeNode
 */
public class TreeNode<T extends TreeNode<T>> implements ITreeNode<T> {

    @Transient
    private List<T> children;

    @Override
    public List<T> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<T> children) {
        this.children = children;
    }

}

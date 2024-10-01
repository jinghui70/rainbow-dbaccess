package io.github.jinghui70.rainbow.utils.tree;

public class WrapTreeNode<T> extends TreeNode<WrapTreeNode<T>> {

    private final T data;

    public WrapTreeNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}

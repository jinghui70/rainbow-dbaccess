package io.github.jinghui70.rainbow.dbaccess.tree;

public class TreeObject<T> extends TreeNode<TreeObject<T>> {

    private final T data;

    private TreeObject<T> parent;

    public TreeObject(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public void setParent(TreeObject<T> parent) {
        this.parent = parent;
    }

    public TreeObject<T> getParent() {
        return this.parent;
    }

    @Override
    public void addChild(TreeObject<T> child) {
        super.addChild(child);
        child.setParent(this);
    }

    @Override
    public String toString() {
        if (data==null) return "null";
        return data.toString();
    }
}
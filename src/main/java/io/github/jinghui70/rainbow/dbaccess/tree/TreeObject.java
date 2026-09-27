package io.github.jinghui70.rainbow.dbaccess.tree;

/**
 * 树节点包装类，把任意对象包成可挂载父子关系的树节点。
 * <p>适用于原始数据类不便实现 {@link ITreeNode} 的场景：数据本身放在 data 里，
 * 父子关系由本类维护。
 *
 * @param <T> 被包装的数据类型
 * @author lijinghui
 */
public class TreeObject<T> extends TreeNode<TreeObject<T>> {

    private final T data;

    private TreeObject<T> parent;

    /**
     * 包装一个数据对象为树节点。
     *
     * @param data 被包装的数据，允许为 null
     */
    public TreeObject(T data) {
        this.data = data;
    }

    /**
     * 获取被包装的数据。
     *
     * @return 被包装的数据
     */
    public T getData() {
        return this.data;
    }

    /**
     * 设置父节点，通常由 {@link #addChild(TreeObject)} 自动维护。
     *
     * @param parent 父节点，根节点为 null
     */
    public void setParent(TreeObject<T> parent) {
        this.parent = parent;
    }

    /**
     * 获取父节点。
     *
     * @return 父节点，根节点返回 null
     */
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
package io.github.jinghui70.rainbow.utils.tree;

/**
 * 树节点消费者接口，用于对树节点进行遍历操作。
 *
 * @param <T> 节点类型，必须是 ITreeNode 接口的实现类
 */
public interface TreeNodeConsumer<T extends ITreeNode<T>> {

    /**
     * 对树节点执行操作。
     *
     * @param node 当前节点
     * @param parentNode 当前节点的父节点
     * @param level 当前节点在树中的层级（根节点为1）
     */
    void accept(T node, T parentNode, int level);
}
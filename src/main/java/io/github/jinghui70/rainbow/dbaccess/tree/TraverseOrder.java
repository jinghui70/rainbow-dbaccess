package io.github.jinghui70.rainbow.dbaccess.tree;

/**
 * 树的遍历顺序。
 */
public enum TraverseOrder {

    /**
     * 前序遍历：先处理当前节点，再处理其子节点。
     */
    PRE_ORDER,

    /**
     * 后序遍历：先处理子节点，再处理当前节点。
     */
    POST_ORDER
}

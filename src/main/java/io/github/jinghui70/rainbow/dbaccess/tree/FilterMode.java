package io.github.jinghui70.rainbow.dbaccess.tree;

/**
 * 树过滤时，对匹配节点的子树如何处理。
 */
public enum FilterMode {

    /**
     * 匹配的节点继续过滤其子树，不匹配的子孙节点会被剪掉。
     */
    FILTER_SUBTREE,

    /**
     * 匹配的节点整棵子树原样保留，不再对子孙节点做判断。
     */
    KEEP_SUBTREE
}

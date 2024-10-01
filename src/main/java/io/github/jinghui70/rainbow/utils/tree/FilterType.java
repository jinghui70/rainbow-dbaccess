package io.github.jinghui70.rainbow.utils.tree;

public enum FilterType {

    MATCH_FIRST, // 首次满足的节点及其上级节点
    MATCH_FIRST_FULL, // 首次满足的节点及其上下级节点
    MATCH_ALL, // 所有满足的节点及其上级节点
    MATCH_ALL_FULL // 满足的节点及所有上下级节点

}
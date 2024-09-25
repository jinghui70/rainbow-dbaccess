package io.github.jinghui70.rainbow.utils;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.function.Consumer;

public class TreeUtils {

    public static <T extends TreeNode<T>> void traverse(T treeNode, Consumer<T> consumer) {
        if (treeNode == null) return;
        consumer.accept(treeNode);
        traverse(treeNode.getChildren(), consumer);
    }

    public static <T extends TreeNode<T>> void traverse(List<T> tree, Consumer<T> consumer) {
        if (CollUtil.isEmpty(tree)) return;
        for (T node : tree) {
            traverse(node, consumer);
        }
    }
}

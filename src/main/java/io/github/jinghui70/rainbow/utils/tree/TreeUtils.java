package io.github.jinghui70.rainbow.utils.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TreeUtils {

    /**
     * Recursively traverse a tree structure and apply a consumer to each node.
     *
     * @param treeNode the root node to traverse
     * @param consumer the consumer to apply to each node
     * @param <T>      the type of the node
     */
    public static <T extends TreeNode<T>> void traverse(T treeNode, Consumer<T> consumer) {
        if (treeNode == null) return;
        consumer.accept(treeNode);
        traverse(treeNode.getChildren(), consumer);
    }

    /**
     * Recursively traverse a tree structure and apply a consumer to each node.
     *
     * @param tree     the root nodes to traverse
     * @param consumer the consumer to apply to each node
     * @param <T>      the type of the node
     */
    public static <T extends TreeNode<T>> void traverse(List<T> tree, Consumer<T> consumer) {
        if (CollUtil.isEmpty(tree)) return;
        for (T node : tree) {
            traverse(node, consumer);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ITreeNode<T>> T cloneWithChildren(T node, List<T> children, Method cloneMethod) {
        if (cloneMethod != null) {
            try {
                node = (T) cloneMethod.invoke(node);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        node.setChildren(children);
        return node;
    }

    private static <T extends ITreeNode<T>> T filter(T node, Predicate<T> predicate, FilterType filterType, Method cloneMethod) {
        boolean hasChild = CollUtil.isNotEmpty(node.getChildren());
        if (predicate.test(node)) {
            switch (filterType) {
                case MATCH_FIRST:
                    return cloneWithChildren(node, null, cloneMethod);
                case MATCH_ALL:
                    if (hasChild) {
                        List<T> children = filter(node.getChildren(), predicate, filterType, cloneMethod);
                        if (CollUtil.isEmpty(children)) children = null;
                        return cloneWithChildren(node, children, cloneMethod);
                    }
                    break;
            }
            return node;
        }
        if (hasChild) {
            List<T> children = filter(node.getChildren(), predicate, filterType, cloneMethod);
            if (CollUtil.isNotEmpty(children)) {
                return cloneWithChildren(node, children, cloneMethod);
            }
        }
        return null;
    }

    private static <T extends ITreeNode<T>> List<T> filter(List<T> list, Predicate<T> predicate, FilterType filterType, Method cloneMethod) {
        List<T> result = new LinkedList<>();
        for (T node : list) {
            T matchNode = filter(node, predicate, filterType, cloneMethod);
            if (matchNode != null) {
                result.add(matchNode);
                if (filterType == FilterType.MATCH_FIRST || filterType == FilterType.MATCH_FIRST_FULL) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Filter a tree-like list according to the given predicate and filter type.
     *
     * @param list       the tree-like list
     * @param predicate  the condition to filter
     * @param filterType the filter type
     * @return the filtered list
     */
    public static <T extends ITreeNode<T>> List<T> filter(List<T> list, Predicate<T> predicate, FilterType filterType) {
        if (CollUtil.isEmpty(list)) return list;
        Method cloneMethod = null;
        if (list.get(0) instanceof Cloneable) {
            Class<?> clazz = list.get(0).getClass();
            try {
                cloneMethod = clazz.getDeclaredMethod("clone");
                cloneMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(StrUtil.format("clone method of {} not defined", clazz.getName()));
            }
        }
        return filter(list, predicate, filterType, cloneMethod);
    }

    public static <T> List<WrapTreeNode<T>> filterWrapTree(List<WrapTreeNode<T>> list, Predicate<T> predicate, FilterType filterType) {
        List<WrapTreeNode<T>> result = new LinkedList<>();
        for (WrapTreeNode<T> wrapNode : list) {
            if (predicate.test(wrapNode.getData())) {
                WrapTreeNode<T> newNode = new WrapTreeNode<>(wrapNode.getData());
                result.add(newNode);
                switch (filterType) {
                    case MATCH_FIRST:
                        return result;
                    case MATCH_FIRST_FULL:
                        newNode.setChildren(wrapNode.getChildren());
                        return result;
                    case MATCH_ALL_FULL:
                        newNode.setChildren(wrapNode.getChildren());
                        break;
                    case MATCH_ALL:
                        if (wrapNode.hasChild()) {
                            List<WrapTreeNode<T>> children = filterWrapTree(wrapNode.getChildren(), predicate, filterType);
                            if (CollUtil.isNotEmpty(children)) newNode.setChildren(children);
                        }
                }
            } else if (wrapNode.hasChild()) {
                List<WrapTreeNode<T>> children = filterWrapTree(wrapNode.getChildren(), predicate, filterType);
                if (CollUtil.isNotEmpty(children)) {
                    WrapTreeNode<T> newNode = new WrapTreeNode<>(wrapNode.getData());
                    newNode.setChildren(children);
                    result.add(newNode);
                }
            }
        }
        return result;
    }

}

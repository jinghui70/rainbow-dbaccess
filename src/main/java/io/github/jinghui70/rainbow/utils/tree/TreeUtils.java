package io.github.jinghui70.rainbow.utils.tree;

import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TreeUtils {

    /**
     * Traverse a tree structure and apply a consumer to each node in pre-order.
     *
     * @param tree   the root nodes to traverse
     * @param action the consumer to apply to each node
     * @param <T>    the type of the node
     */
    public static <T extends ITreeNode<T>> void traverse(List<T> tree, Consumer<T> action) {
        traverse(tree, action, true);
    }

    /**
     * Recursively traverse a tree structure and apply a consumer to each node.
     *
     * @param tree       the root nodes to traverse
     * @param consumer   the consumer to apply to each node
     * @param isPreOrder true: traverse in pre-order; false: traverse in post-order
     * @param <T>        the type of the node
     */
    public static <T extends ITreeNode<T>> void traverse(List<T> tree, Consumer<T> consumer, boolean isPreOrder) {
        if (CollUtil.isEmpty(tree)) return;
        for (T node : tree) {
            traverse(node, consumer, isPreOrder);
        }
    }

    /**
     * 遍历树节点，并对每个节点执行指定的操作。
     *
     * @param <T>        节点类型，必须是 ITreeNode 接口的实现类
     * @param treeNode   要遍历的树节点
     * @param action     对每个节点执行的操作，参数为当前遍历到的节点
     * @param isPreOrder 是否按前序遍历的顺序执行操作
     */
    public static <T extends ITreeNode<T>> void traverse(T treeNode, Consumer<T> action, boolean isPreOrder) {
        if (treeNode == null) return;
        if (isPreOrder) action.accept(treeNode);
        traverse(treeNode.getChildren(), action, isPreOrder);
        if (!isPreOrder) action.accept(treeNode);
    }

    /**
     * 遍历树结构并应用指定操作
     *
     * @param <T>    节点类型，必须实现ITreeNode接口
     * @param tree   待遍历的树结构列表
     * @param action 对每个节点执行的操作
     */
    public static <T extends ITreeNode<T>> void traverse(List<T> tree, TreeNodeConsumer<T> action) {
        traverse(tree, action, true);
    }

    public static <T extends ITreeNode<T>> void traverse(List<T> tree, TreeNodeConsumer<T> action, boolean isPreOrder) {
        for (T node : tree) {
            traverseNode(node, null, 1, action, isPreOrder);
        }
    }

    public static <T extends ITreeNode<T>> void traverseNode(T node, T parentNode, int level, TreeNodeConsumer<T> action, boolean isPreOrder) {
        if (isPreOrder) action.accept(node, parentNode, level);
        if (node.hasChild()) {
            for (T child : node.getChildren()) {
                traverseNode(child, node, level + 1, action, isPreOrder);
            }
        }
        if (!isPreOrder) action.accept(node, parentNode, level);
    }

    public static <F extends ITreeNode<F>, T extends ITreeNode<T>> List<T> transform(List<F> list, Function<F, T> mapper) {
        if (CollUtil.isEmpty(list)) return Collections.emptyList();
        return list.stream().map(node -> transformNode(node, mapper)).collect(Collectors.toList());
    }

    private static <F extends ITreeNode<F>, T extends ITreeNode<T>> T transformNode(F originalNode, Function<F, T> mapper) {
        T result = mapper.apply(originalNode);
        if (originalNode.hasChild()) {
            List<T> children = originalNode.getChildren().stream().map(node -> transformNode(node, mapper)).collect(Collectors.toList());
            result.setChildren(children);
        }
        return result;
    }

    /**
     * 根据给定的条件过滤节点列表。注意，过滤会修改已有节点的children对象，原来的树结构会被破坏。如果要保留原有树结构，请使用TreeNode参数的树。。
     *
     * @param <T>            节点类型，必须实现ITreeNode接口
     * @param nodes          要过滤的节点列表
     * @param predicate      用于筛选节点的条件
     * @param recurseOnMatch 当节点匹配条件时是否递归过滤其子节点
     * @return 过滤后的节点列表
     */
    public static <T extends ITreeNode<T>> List<T> filter(List<T> nodes, Predicate<T> predicate, boolean recurseOnMatch) {
        List<T> result = new ArrayList<>(nodes.size());
        for (T node : nodes) {
            T filtered = filterNode(node, predicate, recurseOnMatch);
            if (Objects.nonNull(filtered)) {
                result.add(filtered);
            }
        }
        return result;
    }


    /**
     * 过滤树节点
     *
     * @param <T>            树节点的类型，必须继承自ITreeNode<T>接口
     * @param node           待过滤的树节点
     * @param predicate      用于判断节点是否符合条件的谓词
     * @param recurseOnMatch 当节点符合条件时是否递归过滤其子节点
     * @return 如果节点符合条件，或其后代节点中有符合的，则放回该节点；否则返回null
     */
    public static <T extends ITreeNode<T>> T filterNode(T node, Predicate<T> predicate, boolean recurseOnMatch) {
        boolean match = predicate.test(node);
        if (match) {
            if (node.hasChild() && recurseOnMatch) {
                List<T> children = filter(node.getChildren(), predicate, recurseOnMatch);
                node = createNewNodeIfTreeObject(node);
                node.setChildren(children.isEmpty() ? null : children);
            }
            return node;
        }
        if (node.hasChild()) {
            List<T> children = filter(node.getChildren(), predicate, recurseOnMatch);
            if (CollUtil.isNotEmpty(children)) {
                node = createNewNodeIfTreeObject(node);
                node.setChildren(children);
                return node;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends ITreeNode<T>> T createNewNodeIfTreeObject(T node) {
        if (node instanceof TreeObject) {
            TreeObject treeObject = (TreeObject) node;
            return (T) new TreeObject(treeObject.getData());
        }
        return node;
    }

    /**
     * 打印树形结构节点列表的字符串表示。
     *
     * @param <T> 节点类型，必须是ITreeNode接口的实现类
     * @param nodes 节点列表，每个节点必须是ITreeNode接口的实现类
     * @return 返回树形结构节点列表的字符串表示
     */
    public static <T extends ITreeNode<T>> String printTree(List<T> nodes) {
        return printTree(nodes, Object::toString);
    }

    /**
     * 打印树形结构。
     *
     * @param <T>           泛型类型，要求实现ITreeNode接口
     * @param nodes         节点列表
     * @param labelFunction 用于获取节点标签的函数
     * @return 树形结构的字符串表示，如果节点列表为空则返回"(empty)"
     */
    public static <T extends ITreeNode<T>> String printTree(List<T> nodes, Function<T, String> labelFunction) {
        if (CollUtil.isEmpty(nodes)) return "(empty)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            boolean isLast = i == nodes.size() - 1;
            printNode(sb, nodes.get(i), labelFunction, "", isLast);
        }
        return sb.toString();
    }

    private static <T extends ITreeNode<T>> void printNode(StringBuilder sb, T
            node, Function<T, String> labelFunction, String prefix, boolean isLast) {
        sb.append(prefix).append(isLast ? "└── " : "├── ").append(labelFunction.apply(node)).append("\n");
        // 如果有子节点，递归打印
        if (node.hasChild()) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            List<T> children = node.getChildren();
            for (int i = 0; i < node.getChildren().size(); i++) {
                boolean isChildLast = i == children.size() - 1;
                printNode(sb, children.get(i), labelFunction, newPrefix, isChildLast);
            }
        }
    }
}
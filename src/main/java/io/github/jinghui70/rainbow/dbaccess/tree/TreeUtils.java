package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树结构工具类，提供树的遍历、转换、过滤、打印等常用操作。
 */
public class TreeUtils {

    /**
     * 遍历树结构并对每个节点执行指定操作。
     *
     * @param <T>       节点类型，必须实现ITreeNode接口
     * @param treeNodes 待遍历的树结构列表，为空时不做任何处理
     * @param action    对每个节点执行的操作
     * @param order     遍历顺序
     */
    public static <T extends ITreeNode<T>> void traverse(List<T> treeNodes, Consumer<T> action, TraverseOrder order) {
        if (CollUtil.isEmpty(treeNodes)) return;
        for (T node : treeNodes) {
            traverse(node, action, order);
        }
    }

    /**
     * 遍历单个树节点及其子孙节点，并对每个节点执行指定操作。
     *
     * @param <T>      节点类型，必须实现ITreeNode接口
     * @param treeNode 待遍历的树节点，为null时不做任何处理
     * @param action   对每个节点执行的操作，参数为当前遍历到的节点
     * @param order    遍历顺序
     */
    public static <T extends ITreeNode<T>> void traverse(T treeNode, Consumer<T> action, TraverseOrder order) {
        if (treeNode == null) return;
        if (order == TraverseOrder.PRE_ORDER) action.accept(treeNode);
        traverse(treeNode.getChildren(), action, order);
        if (order == TraverseOrder.POST_ORDER) action.accept(treeNode);
    }

    /**
     * 遍历树结构并对每个节点执行指定操作，操作可以获得父节点和层级。
     *
     * @param <T>       节点类型，必须实现ITreeNode接口
     * @param treeNodes 待遍历的树结构列表，为空时不做任何处理
     * @param action    对每个节点执行的操作
     * @param order     遍历顺序
     */
    public static <T extends ITreeNode<T>> void traverse(List<T> treeNodes, TreeNodeConsumer<T> action, TraverseOrder order) {
        if (CollUtil.isEmpty(treeNodes)) return;
        for (T node : treeNodes) {
            traverseNode(node, null, 1, action, order);
        }
    }

    /**
     * 递归遍历树节点并应用指定操作。
     *
     * @param <T>        节点类型，必须实现ITreeNode接口
     * @param node       当前遍历的节点，为null时不做任何处理
     * @param parentNode 父节点
     * @param level      当前节点层级（根节点为1）
     * @param action     对每个节点执行的操作
     * @param order      遍历顺序
     */
    public static <T extends ITreeNode<T>> void traverseNode(T node, T parentNode, int level, TreeNodeConsumer<T> action, TraverseOrder order) {
        if (node == null) return;
        if (order == TraverseOrder.PRE_ORDER) action.accept(node, parentNode, level);
        if (node.hasChild()) {
            for (T child : node.getChildren()) {
                traverseNode(child, node, level + 1, action, order);
            }
        }
        if (order == TraverseOrder.POST_ORDER) action.accept(node, parentNode, level);
    }

    /**
     * 将树结构从一种节点类型转换为另一种节点类型。
     *
     * @param <F>    源节点类型
     * @param <T>    目标节点类型
     * @param list   源树结构列表，为空时返回空列表
     * @param mapper 节点转换函数
     * @return 转换后的树结构列表，可修改
     */
    public static <F extends ITreeNode<F>, T extends ITreeNode<T>> List<T> transform(List<F> list, Function<F, T> mapper) {
        if (CollUtil.isEmpty(list)) return new ArrayList<>();
        return list.stream().map(node -> transformNode(node, mapper)).collect(Collectors.toList());
    }

    /**
     * 递归转换单个节点及其子节点。
     *
     * @param <F>          源节点类型
     * @param <T>          目标节点类型
     * @param originalNode 源节点
     * @param mapper       节点转换函数
     * @return 转换后的节点
     */
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
     * @param <T>       节点类型，必须实现ITreeNode接口
     * @param nodes     要过滤的节点列表，为空时返回空列表
     * @param predicate 用于筛选节点的条件
     * @param mode      匹配节点的子树如何处理
     * @return 过滤后的节点列表，可修改
     */
    public static <T extends ITreeNode<T>> List<T> filter(List<T> nodes, Predicate<T> predicate, FilterMode mode) {
        if (CollUtil.isEmpty(nodes)) return new ArrayList<>();
        List<T> result = new ArrayList<>(nodes.size());
        for (T node : nodes) {
            T filtered = filterNode(node, predicate, mode);
            if (Objects.nonNull(filtered)) {
                result.add(filtered);
            }
        }
        return result;
    }

    /**
     * 过滤树节点
     *
     * @param <T>       树节点的类型，必须继承自ITreeNode&lt;T&gt;接口
     * @param node      待过滤的树节点，为null时返回null
     * @param predicate 用于判断节点是否符合条件的谓词
     * @param mode      匹配节点的子树如何处理
     * @return 如果节点符合条件，或其后代节点中有符合的，则放回该节点；否则返回null
     */
    public static <T extends ITreeNode<T>> T filterNode(T node, Predicate<T> predicate, FilterMode mode) {
        if (node == null) return null;
        boolean match = predicate.test(node);
        if (match) {
            if (node.hasChild() && mode == FilterMode.FILTER_SUBTREE) {
                List<T> children = filter(node.getChildren(), predicate, FilterMode.FILTER_SUBTREE);
                node.setChildren(children.isEmpty() ? null : children);
            }
            return node;
        }
        if (node.hasChild()) {
            List<T> children = filter(node.getChildren(), predicate, mode);
            if (CollUtil.isNotEmpty(children)) {
                node.setChildren(children);
                return node;
            }
        }
        return null;
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
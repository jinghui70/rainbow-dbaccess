package io.github.jinghui70.rainbow.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class WrapTreeNode<T> extends TreeNode<WrapTreeNode<T>> {

    private final T data;

    public WrapTreeNode(T data) {
        this.data = data;
    }

    public static <T> List<WrapTreeNode<T>> filter(List<WrapTreeNode<T>> list, Predicate<T> predicate, FilterType filterType) {
        List<WrapTreeNode<T>> result = new LinkedList<>();
        for (WrapTreeNode<T> wrapNode : list) {
            if (predicate.test(wrapNode.data)) {
                WrapTreeNode<T> newNode = new WrapTreeNode<>(wrapNode.data);
                result.add(newNode);
                if (filterType != FilterType.FIRST_MATCH && wrapNode.hasChild()) {
                    List<WrapTreeNode<T>> newChildren = filterType == FilterType.ALL_MATCH ?
                            filter(wrapNode.getChildren(), predicate, filterType) :
                            filter(wrapNode.getChildren(), t -> true, FilterType.ALL_MATCH);
                    newNode.setChildren(newChildren);
                }
            } else if (wrapNode.hasChild()) {
                List<WrapTreeNode<T>> newChildren = filter(wrapNode.getChildren(), predicate, filterType);
                if (newChildren != null) {
                    WrapTreeNode<T> newNode = new WrapTreeNode<>(wrapNode.data);
                    newNode.setChildren(newChildren);
                    result.add(newNode);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    public T getData() {
        return data;
    }
}

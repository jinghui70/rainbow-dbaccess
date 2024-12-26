package io.github.jinghui70.rainbow.utils.tree;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.Map;

public class Tree<T>  {

    private T root;

    private final List<T> roots;

    private final Map<String, T> nodeMap;

    public Tree(List<T> roots, Map<String, T> nodeMap) {
        this.roots = roots;
        if (CollUtil.isNotEmpty(roots) && roots.size()==1) {
            this.root = roots.get(0);
        }
        this.nodeMap = nodeMap;
    }

    public T getRoot() {
        return root;
    }

    public List<T> getRoots() {
        return roots;
    }

    public Map<String, T> getNodeMap() {
        return nodeMap;
    }

    public T getRoot(int index) {
        return roots.get(index);
    }

    public T getNode(String key) {
        return nodeMap.get(key);
    }
}

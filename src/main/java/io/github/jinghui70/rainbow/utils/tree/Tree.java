package io.github.jinghui70.rainbow.utils.tree;

import java.util.List;
import java.util.Map;

public class Tree<T> {

    private final List<T> roots;

    private final Map<String, T> nodeMap;

    /**
     * 构造方法，用于创建Tree对象
     *
     * @param roots   根节点列表，包含所有根节点
     * @param nodeMap 节点映射，键为节点标识，值为节点对象
     */
    public Tree(List<T> roots, Map<String, T> nodeMap) {
        this.roots = roots;
        this.nodeMap = nodeMap;
    }

    /**
     * 获取根元素列表
     *
     * @return 包含所有根元素的列表
     */
    public List<T> getRoots() {
        return roots;
    }

    /**
     * 获取节点映射表
     *
     * @return 包含所有节点的映射表，键为节点标识，值为节点对象
     */
    public Map<String, T> getNodeMap() {
        return nodeMap;
    }

    /**
     * 根据节点标识获取节点
     *
     * @param key 节点的唯一标识符
     * @return 返回指定标识符的节点对象
     */
    public T getNode(String key) {
        return nodeMap.get(key);
    }

}

package io.github.jinghui70.rainbow.utils.tree;

import java.util.List;
import java.util.Map;

public class WrapTree<T> extends Tree<TreeObject<T>> {

    public WrapTree(List<TreeObject<T>> roots, Map<String,TreeObject<T>> nodeMap) {
        super(roots, nodeMap);
    }
}

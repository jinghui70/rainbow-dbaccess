package io.github.jinghui70.rainbow.utils.tree;

import java.util.List;

public interface ITreeNode<T extends ITreeNode<T>> {

    public List<T> getChildren();

    public void setChildren(List<T> children);

    public void addChild(T child);
}

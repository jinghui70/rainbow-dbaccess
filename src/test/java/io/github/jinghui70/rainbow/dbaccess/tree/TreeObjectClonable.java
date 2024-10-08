package io.github.jinghui70.rainbow.dbaccess.tree;

import io.github.jinghui70.rainbow.utils.tree.TreeNode;

public class TreeObjectClonable extends TreeNode<TreeObjectClonable> implements Cloneable {

    private String id;

    private String pid;

    private String code;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TreeObjectClonable clone() {
        try {
            return (TreeObjectClonable) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

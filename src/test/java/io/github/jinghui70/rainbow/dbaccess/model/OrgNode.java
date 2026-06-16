package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;
import io.github.jinghui70.rainbow.dbaccess.tree.ITreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形组织节点，测试 queryForTree / queryForWrapTree。
 */
@Table(name = "T_ORG")
public class OrgNode implements ITreeNode<OrgNode> {

    private String id;
    private String pid;
    private String name;
    private String code;
    @Transient
    private List<OrgNode> children = new ArrayList<>();

    public OrgNode() {
    }

    public OrgNode(String id, String pid, String name, String code) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.code = code;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public List<OrgNode> getChildren() { return children; }

    @Override
    public void addChild(OrgNode child) { this.children.add(child); }

    @Override
    public void setChildren(List<OrgNode> children) { this.children = children; }

    public String toString() {
        return code + " " + name;
    }
}
